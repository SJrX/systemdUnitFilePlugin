package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValidInspectionForImagePolicy : AbstractUnitFileTest() {

  fun testNoWarningWhenNumberSpecifiedWithoutUnit() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           RootImagePolicy=root=unprotected
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)

  }

  fun testWeakWarningWhenInvalidPartitionTypeIsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           RootImagePolicy=opt=unprotected
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("RootImagePolicy's value is correctly format but seems invalid", info!!.description)
    TestCase.assertEquals("opt", info.text)
  }

  fun testWeakWarningWhenInvalidPolicyFlagIsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           RootImagePolicy=root=unsigned
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("RootImagePolicy's value is correctly format but seems invalid", info!!.description)
    TestCase.assertEquals("unsigned", info.text)
  }

  fun testWeakWarningWhenInvalidPolicyFlagIsSpecifiedInSecondaryPolicy() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           RootImagePolicy=home=encrypted+used
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]

    AbstractUnitFileTest.Companion.assertStringContains("RootImagePolicy's value is correctly format but seems invalid", info!!.description)
    TestCase.assertEquals("used", info.text)
    assertContainsQuickfix(info, "Replace 'used' with 'absent'")
  }

  fun testWeakWarningWhenIncompletePolicyFlagIsSpecifiedInSecondaryPolicy() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           RootImagePolicy=home=encrypted+absent+
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("RootImagePolicy's value does not match the expected format. Possible reasons include unrecognized characters or premature end of input", info!!.description)
    TestCase.assertEquals("home=encrypted+absent+", info.text)
  }

  fun testWeakWarningWhenInvalidPolicySetInSecondPolicy() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           RootImagePolicy=home=encrypted:root=encrypted+used
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("RootImagePolicy's value is correctly format but seems invalid", info!!.description)
    TestCase.assertEquals("used", info.text)
    assertContainsQuickfix(info, "Replace 'used' with 'absent'")
  }

}
