package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValidInspectionForAllowedCpuSetOptionValueTest : AbstractUnitFileTest() {

  fun testNoWeakWarningWhenSingleIntegerSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           AllowedCPUs=1
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWeakWarningWhenTwoIntegersSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           AllowedCPUs=1 3
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWeakWarningWhenValidRangeSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           AllowedCPUs=1-4
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWeakWarningWhenTwoValidRangesSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           AllowedCPUs=1-4 5-8
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }


  fun testNoWeakWarningAllSyntaxesAreMissed() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           AllowedCPUs=1,2 3 5-8,10-200  245
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
           AllowedCPUs=-1
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("Invalid syntax for CPU Range", info!!.description)
    TestCase.assertEquals("-1", info.text)
  }

  fun testWeakWarningWhenNegativeIntegerUsedInRange() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           AllowedCPUs=1--10
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("Invalid syntax for CPU Range", info!!.description)
    TestCase.assertEquals("1--10", info.text)
  }

  fun testWeakWarningWhenRangeIsBackwardsRange() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           AllowedCPUs=8-5
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("The start 8 of the range must be less than the end 5 of the range", info!!.description)
    TestCase.assertEquals("8-5", info.text)
  }

  fun testWeakWarningWhenMultipleDashesAreUsed() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           AllowedCPUs=1-2-3
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("Invalid syntax for CPU Range", info!!.description)
    TestCase.assertEquals("1-2-3", info.text)
  }

  fun testWeakWarningWhenAWeirdStringIsUsed() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           AllowedCPUs=1-yo
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("Invalid syntax for CPU Range", info!!.description)
    TestCase.assertEquals("1-yo", info.text)
  }






}
