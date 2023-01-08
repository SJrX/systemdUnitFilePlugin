package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionForExecOptionsTest : AbstractUnitFileTest() {

  fun testWeakWarningWhenUsingNonAbsolutePathForExecOptions() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           ExecStartPre=docker-compose
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("an absolute path", info!!.description)
    TestCase.assertEquals("docker-compose", info.text)
  }

  fun testWeakWarningWhenUsingNonAbsolutePathForExecOptionsAndWhitespaceInFront() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStopPost=             docker-compose
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("an absolute path", info!!.description)
    TestCase.assertEquals("docker-compose", info.text)
  }

  fun testNoWarningWithAbsolutePathWithWhitespace() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStopPost=             /sbin/docker-compose
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWithAbsolutePathWithAtPrefix() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=@/sbin/docker-compose
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWithAbsolutePathWithDashPrefix() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=-/sbin/docker-compose
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWithAbsolutePathWithColonPrefix() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=:/sbin/docker-compose
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWithAbsolutePathWithPlusPrefix() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=+/sbin/docker-compose
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWithAbsolutePathWithExclamationPrefix() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=!/sbin/docker-compose
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWithAbsolutePathWithDoubleExclamationPointPrefix() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=!!/sbin/docker-compose
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWithAbsolutePathWithABunchOfPrefixesPrefix() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=@-:!!/sbin/docker-compose
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWeakWarningWhenUsingNonAbsolutePathWithWhitespaceAndAtCharacterPrefix() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStopPost=             @docker-compose
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("an absolute path", info!!.description)
    TestCase.assertEquals("docker-compose", info.text)
  }

  fun testWeakWarningWhenUsingNonAbsolutePathWithWhitespaceAndDashCharacterPrefix() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStopPost=             -docker-compose
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("an absolute path", info!!.description)
    TestCase.assertEquals("docker-compose", info.text)
  }

  fun testWeakWarningWhenUsingNonAbsolutePathWithWhitespaceAndColonCharacterPrefix() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStopPost=             :docker-compose
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("an absolute path", info!!.description)
    TestCase.assertEquals("docker-compose", info.text)
  }

  fun testWeakWarningWhenUsingNonAbsolutePathWithWhitespaceAndPlusCharacterPrefix() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStopPost=             +docker-compose
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("an absolute path", info!!.description)
    TestCase.assertEquals("docker-compose", info.text)
  }

  fun testWeakWarningWhenUsingNonAbsolutePathWithWhitespaceAndExclamationCharacterPrefix() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStopPost=             !docker-compose
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("an absolute path", info!!.description)
    TestCase.assertEquals("docker-compose", info.text)
  }

  fun testWeakWarningWhenUsingNonAbsolutePathWithWhitespaceAndDoubleExclamationCharacterPrefix() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStopPost=            !!docker-compose
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("an absolute path", info!!.description)
    TestCase.assertEquals("docker-compose", info.text)
  }

  fun testWeakWarningWhenUsingNonAbsolutePathWithWhitespaceAndABunchOfPrefixes() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStopPost=            @-:!!docker-compose
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("an absolute path", info!!.description)
    TestCase.assertEquals("docker-compose", info.text)
  }
}
