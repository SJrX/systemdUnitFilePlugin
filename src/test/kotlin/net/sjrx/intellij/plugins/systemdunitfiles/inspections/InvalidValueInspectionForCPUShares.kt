package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionForCPUShares : AbstractUnitFileTest() {


  fun testNoWarningWhenTwoSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           CPUShares=2
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenMaxValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           CPUShares=262144
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWeakWarningWhenOneIsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           CPUShares=1
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("CPUShares's value is correctly formatted but seems invalid", info!!.description)
    TestCase.assertEquals("1", info.text)
  }

  fun testWeakWarningWhenNegativeTenIsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           CPUShares=-10
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("CPUShares's value is correctly formatted but seems invalid", info!!.description)
    TestCase.assertEquals("-10", info.text)
  }


  fun testWeakWarningWhenValueTooBigIsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           CPUShares=262145
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("CPUShares's value is correctly formatted but seems invalid", info!!.description)
    TestCase.assertEquals("262145", info.text)
  }

}
