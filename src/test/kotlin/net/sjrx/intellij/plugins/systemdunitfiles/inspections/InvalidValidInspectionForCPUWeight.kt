package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValidInspectionForCPUWeight : AbstractUnitFileTest() {

  fun testNoWarningWhenIdleSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           CPUWeight=idle
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenOneSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           CPUWeight=1
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenTenThousandSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           CPUWeight=10000
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWeakWarningWhenZeroIsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           CPUWeight=0
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("CPUWeight's value is correctly format but seems invalid", info!!.description)
    TestCase.assertEquals("0", info.text)
  }

  fun testWeakWarningWhenNegativeTenIsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           CPUWeight=-10
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("CPUWeight's value is correctly format but seems invalid", info!!.description)
    TestCase.assertEquals("-10", info.text)
  }


  fun testWeakWarningWhenNegativeHundredThousandIsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           CPUWeight=-100000
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("CPUWeight's value is correctly format but seems invalid", info!!.description)
    TestCase.assertEquals("-100000", info.text)
  }

}
