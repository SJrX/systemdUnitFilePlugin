package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionForSignalOptionsTest : AbstractUnitFileTest() {

  fun testWarningWhenUsingUnrecognizedKillSignalValue() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      [Service]
      KillSignal=SIGARRET
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("signal(7) man page", info!!.description)
    TestCase.assertEquals("SIGARRET", info.text)
  }

  fun testWarningWhenUsingLowercasedKillSignalValue() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      [Service]
      KillSignal=sigkill
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("signal(7) man page", info!!.description)
    TestCase.assertEquals("sigkill", info.text)
  }

  fun testWarningWhenUsingOutOfRangeIntegerKillSignalValue() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      [Service]
      KillSignal=128
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("if using an integer", info!!.description)
    TestCase.assertEquals("128", info.text)
  }

  fun testNoWarningWhenUsingValidKillSignalValues() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      [Service]
      KillSignal=SIGINT
      FinalKillSignal=9
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }
}
