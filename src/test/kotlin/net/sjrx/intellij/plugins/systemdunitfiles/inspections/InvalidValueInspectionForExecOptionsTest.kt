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
    assertStringContains("an absolute path", info!!.description)
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
    assertStringContains("an absolute path", info!!.description)
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
    assertStringContains("an absolute path", info!!.description)
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
    assertStringContains("an absolute path", info!!.description)
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
    assertStringContains("an absolute path", info!!.description)
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
    assertStringContains("an absolute path", info!!.description)
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
    assertStringContains("an absolute path", info!!.description)
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
    assertStringContains("an absolute path", info!!.description)
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
    assertStringContains("an absolute path", info!!.description)
    TestCase.assertEquals("docker-compose", info.text)
  }

  fun testNoWarningWhenUsingHomeDirectorySpecifier() {
    // Regression test for GitHub issue #529: %h is a valid systemd specifier that expands to the user's home
    // directory (an absolute path), so ExecStart=%h/... must not be flagged.
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=%h/path/to/executable

           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenUsingRuntimeDirectorySpecifierWithDashPrefix() {
    // %t expands to the runtime directory root (an absolute path); the dash prefix must not change that.
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=-%t/foo/bar

           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenUsingUserShellSpecifier() {
    // %s expands to the user's shell (an absolute path such as /bin/bash).
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=%s

           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWeakWarningWhenUsingNonAbsolutePathSpecifier() {
    // %n (the unit name) is NOT guaranteed to be an absolute path, so the recommendation should still fire.
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=%n/path/to/executable

           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("an absolute path", info!!.description)
    TestCase.assertEquals("%n/path/to/executable", info.text)
  }

  fun testNoWarningForAnyAbsolutePathSpecifier() {
    // Every specifier that systemd's unit_path_printf() expands to an absolute path must suppress the warning.
    // These are the full whitelist from ExecOptionValue.ABSOLUTE_PATH_SPECIFIERS (h s C d D E L S t T V y Y f);
    // keep the two in sync.
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           ExecStart=%h/path/to/executable
           ExecStart=%s/path/to/executable
           ExecStart=%C/path/to/executable
           ExecStart=%d/path/to/executable
           ExecStart=%D/path/to/executable
           ExecStart=%E/path/to/executable
           ExecStart=%L/path/to/executable
           ExecStart=%S/path/to/executable
           ExecStart=%t/path/to/executable
           ExecStart=%T/path/to/executable
           ExecStart=%V/path/to/executable
           ExecStart=%y/path/to/executable
           ExecStart=%Y/path/to/executable
           ExecStart=%f/path/to/executable

           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWeakWarningForNonAbsolutePathSpecifiers() {
    // Specifiers that expand to arbitrary text (names, host/OS facts, user/group) are NOT absolute paths, so the
    // recommendation must still fire once per line. None of these appears in ExecOptionValue.ABSOLUTE_PATH_SPECIFIERS.
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           ExecStart=%i/path/to/executable
           ExecStart=%I/path/to/executable
           ExecStart=%n/path/to/executable
           ExecStart=%N/path/to/executable
           ExecStart=%p/path/to/executable
           ExecStart=%P/path/to/executable
           ExecStart=%H/path/to/executable
           ExecStart=%l/path/to/executable
           ExecStart=%m/path/to/executable
           ExecStart=%M/path/to/executable
           ExecStart=%u/path/to/executable
           ExecStart=%U/path/to/executable
           ExecStart=%a/path/to/executable
           ExecStart=%b/path/to/executable
           ExecStart=%v/path/to/executable

           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(15, highlights)
  }
}
