package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionForExecDirectoriesOptionsTest : AbstractUnitFileTest() {

  fun testNoWarningWhenUsingASingleRelativeDirectory() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           RuntimeDirectory=foo
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenUsingSeveralRelativeDirectories() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           RuntimeDirectory=foo bar \
           hello
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUsingASingleAbsoluteDirectory() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           RuntimeDirectory=/tmp/myunit
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("a list of relative paths", info!!.description)
    TestCase.assertEquals("/tmp/myunit", info.text)
  }

  fun testWarningWhenUsingSeveralAbsoluteDirectory() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           RuntimeDirectory=/tmp/myunit /tmp/myunit2
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("Takes a list of relative paths", info!!.description)
    TestCase.assertEquals("/tmp/myunit /tmp/myunit2", info.text)
  }

  fun testWarningWhenUsingParentDirectoryOperator() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           RuntimeDirectory=../myunit
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("Path cannot contain `..`", info!!.description)
    TestCase.assertEquals("../myunit", info.text)
  }

  fun testWarningWhenUsingParentDirectoryOperatorInAThirdDirectory() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           RuntimeDirectory=foo/bar test/tmp/ ../myunit
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("Path cannot contain `..`", info!!.description)
    TestCase.assertEquals("foo/bar test/tmp/ ../myunit", info.text)
  }
}
