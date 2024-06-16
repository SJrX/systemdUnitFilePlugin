package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValidInspectionForTTYSize : AbstractUnitFileTest() {

  fun testWeakWarningWhenNegativeIntegerSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           TTYRows=-5
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("must be an unsigned integer", info!!.description)
    TestCase.assertEquals("-5", info.text)
  }

  fun testWeakWarningWhenStringSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           TTYRows=foo
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("must be an unsigned integer", info!!.description)
    TestCase.assertEquals("foo", info.text)
  }

  fun testNoWeakWarningWhenUnsignedIntegerSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           TTYRows=2
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

}
