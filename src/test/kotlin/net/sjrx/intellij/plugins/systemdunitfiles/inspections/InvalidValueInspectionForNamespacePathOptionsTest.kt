package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionForNamespacePathOptionsTest : AbstractUnitFileTest() {

  fun testNoWarningsWhenUsingAbsolutePath() {
    // Fixture Setup
    //language="unit file (systemd)"
    val file = """
     [Service]
     ReadWritePaths=/foo/bar
     """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningsWhenUsingAbsolutePathWithEscapedSpace() {
    // Fixture Setup
    //language="unit file (systemd)"
    val file = """
     [Service]
     ReadWritePaths=/foo\ zoo/bar
     """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningsWhenUsingAbsolutePathWithPlusPrefix() {
    // Fixture Setup
    //language="unit file (systemd)"
    val file = """
     [Service]
     ReadWritePaths=+/foo/bar
     """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningsWhenUsingAbsolutePathWithMinusPrefix() {
    // Fixture Setup
    //language="unit file (systemd)"
    val file = """
     [Service]
     ReadWritePaths=-/foo/bar
     """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningsWhenUsingAbsolutePathWithMinusThenPlusPrefix() {
    // Fixture Setup
    //language="unit file (systemd)"
    val file = """
     [Service]
     ReadWritePaths=-+/foo/bar
     """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningsWhenUsingAbsolutePathWithPlusThenMinusPrefix() {
    // Fixture Setup
    //language="unit file (systemd)"
    val file = """
     [Service]
     ReadWritePaths=+-/foo/bar
     """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("- must be before +", info!!.description)
    TestCase.assertEquals("+-", info.text)
  }

  fun testWarningsWhenUsingRelativePath() {
    // Fixture Setup
    //language="unit file (systemd)"
    val file = """
     [Service]
     ReadWritePaths=foo/bar
     """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("all paths must be absolute", info!!.description)
    TestCase.assertEquals("foo/bar", info.text)
  }

  fun testNoWarningsWhenUsingAbsolutePathAfterValidPath() {
    // Fixture Setup
    //language="unit file (systemd)"
    val file = """
     [Service]
     ReadWritePaths=/var/spool/ /foo/bar
     """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningsWhenUsingAbsolutePathWithPlusPrefixAfterValidPath() {
    // Fixture Setup
    //language="unit file (systemd)"
    val file = """
     [Service]
     ReadWritePaths=/var/spool/ +/foo/bar
     """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningsWhenUsingAbsolutePathWithMinusPrefixAfterValidPath() {
    // Fixture Setup
    //language="unit file (systemd)"
    val file = """
     [Service]
     ReadWritePaths=/var/spool/ -/foo/bar
     """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningsWhenUsingAbsolutePathWithMinusThenPlusPrefixAfterValidPath() {
    // Fixture Setup
    //language="unit file (systemd)"
    val file = """
     [Service]
     ReadWritePaths=/var/spool/ -+/foo/bar
     """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningsWhenUsingAbsolutePathWithPlusThenMinusPrefixAfterValidPath() {
    // Fixture Setup
    //language="unit file (systemd)"
    val file = """
     [Service]
     ReadWritePaths=/var/spool/ +-/foo/bar
     """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("- must be before +", info!!.description)
    TestCase.assertEquals("+-", info.text)
  }

  fun testWarningsWhenUsingRelativePathAfterValidPath() {
    // Fixture Setup
    //language="unit file (systemd)"
    val file = """
     [Service]
     ReadWritePaths=/var/spool/ foo/bar
     """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("all paths must be absolute", info!!.description)
    TestCase.assertEquals("foo/bar", info.text)
  }

  fun testWarningsWithComplexCase() {
    // Fixture Setup
    //language="unit file (systemd)"
    val file = """
     [Service]
     #                             BBB                BBBBBBBBBBB BBBBBBBBB              BBB       BB                  BBBBB                   BBBBBBBBBBBBBBBB                      BBBBBBBBBBBBBBBBB            BBBBBBBBBB 
     ReadWritePaths=   /var/spool/ bin /boot    /etc/ bad/example home/user /media +/tmp foo -/run +-/sbin -+/srv /dev lib64 /home/John\ Smith home/Jane\ Smith -+/home/user\ two    +home/user\ three /proc/cpus +-proc/mem
     """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(10, highlights)
    var info = highlights[0]
    assertStringContains("all paths must be absolute", info!!.description)
    TestCase.assertEquals("bin", info.text)

    info = highlights[1]
    assertStringContains("all paths must be absolute", info!!.description)
    TestCase.assertEquals("bad/example", info.text)

    info = highlights[2]
    assertStringContains("all paths must be absolute", info!!.description)
    TestCase.assertEquals("home/user", info.text)

    info = highlights[3]
    assertStringContains("all paths must be absolute", info!!.description)
    TestCase.assertEquals("foo", info.text)

    info = highlights[4]
    assertStringContains("Invalid Prefix [+-]", info!!.description)
    TestCase.assertEquals("+-", info.text)

    info = highlights[5]
    assertStringContains("all paths must be absolute", info!!.description)
    TestCase.assertEquals("lib64", info.text)

    info = highlights[6]
    assertStringContains("all paths must be absolute", info!!.description)
    TestCase.assertEquals("home/Jane\\ Smith", info.text)

    info = highlights[7]
    assertStringContains("all paths must be absolute", info!!.description)
    TestCase.assertEquals("home/user\\ three", info.text)

    info = highlights[8]
    assertStringContains("Invalid Prefix [+-]", info!!.description)
    TestCase.assertEquals("+-", info.text)

    info = highlights[9]
    assertStringContains("all paths must be absolute", info!!.description)
    TestCase.assertEquals("proc/mem", info.text)
  }
}
