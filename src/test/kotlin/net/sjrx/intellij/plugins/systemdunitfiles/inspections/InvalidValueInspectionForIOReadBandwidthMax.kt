package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionForIOReadBandwidthMax : AbstractUnitFileTest() {

  fun testNoWarningWhenNumberSpecifiedWithoutUnit() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           IOReadBandwidthMax=/home 2
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)

  }

  fun testNoWarningWhenNumberSpecifiedWithUnit() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           IOReadBandwidthMax=/home 2M
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)

  }

  fun testWeakWarningWhenNegativeIntegerSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           IOReadBandwidthMax=-5
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("IOReadBandwidthMax's value does not match the expected format. Possible reasons include unrecognized characters or premature end of input.", info!!.description)
    TestCase.assertEquals("-5", info.text)
  }

  fun testWeakWarningWhenDeviceSpecifiedWithNoValue() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           IOReadBandwidthMax=/home
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("IOReadBandwidthMax's value does not match the expected format. Possible reasons include unrecognized characters or premature end of input.", info!!.description)
    TestCase.assertEquals("/home", info.text)
  }


  fun testWeakWarningWhenPositiveIntegerSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           IOReadBandwidthMax=5
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("IOReadBandwidthMax's value does not match the expected format. Possible reasons include unrecognized characters or premature end of input.", info!!.description)
    TestCase.assertEquals("5", info.text)
  }

}
