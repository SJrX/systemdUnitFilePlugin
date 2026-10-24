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
    assertStringContains("Takes one of", info!!.description)
    TestCase.assertEquals("foo", info.text)
  }

  fun testNoWarningWhenSecondArgumentForFdIsEmpty() {
    // config_parse_exec_output() accepts an empty fd: name (it then uses the default name); the test used to assert
    // the opposite. https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1354-L1369
    runValidValueTest("fd:")
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
    assertStringContains("path must be specified after the colon", info!!.description)
    TestCase.assertEquals("file:", info.text)
  }

}
