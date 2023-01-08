package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValidInspectionForPathOptionValueTest : AbstractUnitFileTest() {

  fun testWeakWarningWhenNegativeIntegerSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           USBFunctionDescriptors=foo
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("absolute path", info!!.description)
    TestCase.assertEquals("foo", info.text)
  }

  fun testNoWeakWarningWhenStringSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           USBFunctionDescriptors=/foo
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

}
