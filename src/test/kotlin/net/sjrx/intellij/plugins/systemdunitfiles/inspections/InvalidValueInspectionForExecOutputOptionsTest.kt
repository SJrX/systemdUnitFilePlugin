package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionForExecOutputOptionsTest : AbstractUnitFileTest() {


  fun testNoWarningWhenUsingValidValuesForOutput() {
    runValidValueTest("inherit")
    runValidValueTest("null")
    runValidValueTest("tty")
    runValidValueTest("journal")
    runValidValueTest("kmsg")
    runValidValueTest("journal+console")
    runValidValueTest("kmsg+console")
    runValidValueTest("file:/dev/null")
    runValidValueTest("append:/tmp/log.txt")
    runValidValueTest("truncate:/tmp/path")
    runValidValueTest("socket")
    runValidValueTest("fd:stdout")
  }

  fun runValidValueTest(validValue : String) {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           StandardOutput=$validValue
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }


  fun testWeakWarningWhenUsingInvalidValueForExecOption() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           StandardOutput=foo
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("Takes one of", info!!.description)
    TestCase.assertEquals("foo", info.text)
  }

  fun testWeakWarningWhenMissingSecondArgumentForFd() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           StandardOutput=fd:
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("name must be specified after the colon", info!!.description)
    TestCase.assertEquals("fd:", info.text)
  }

  fun testWeakWarningWhenMissingSecondArgumentForFile() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           StandardOutput=file:
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("path must be specified after the colon", info!!.description)
    TestCase.assertEquals("file:", info.text)
  }

}
