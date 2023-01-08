package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValidInspectionForUnsignedInteger : AbstractUnitFileTest() {

  fun testWeakWarningWhenNegativeIntegerSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Swap]
           LogRateLimitBurst=-5
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.swap", file)
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
           [Swap]
           LogRateLimitBurst=foo
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.swap", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("must be an unsigned integer", info!!.description)
    TestCase.assertEquals("foo", info.text)
  }

  fun testNoWeakWarningWhenStringSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Swap]
           LogRateLimitBurst=2
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.swap", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

}
