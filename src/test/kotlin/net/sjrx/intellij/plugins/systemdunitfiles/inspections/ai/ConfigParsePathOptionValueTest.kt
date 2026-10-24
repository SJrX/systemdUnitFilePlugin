package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParsePathOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // config_parse_path: literal absolute path, no '..' component, no API VFS restriction.
    // language="unit file (systemd)"
    val file = """
      [Exec]
      WorkingDirectory=/
      WorkingDirectory=/root
      WorkingDirectory=/home/user/work dir
      WorkingDirectory=/srv//data/./x
      WorkingDirectory=/..foo/bar..
    """.trimIndent()

    setupFileInEditor("file.nspawn", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Exec]
      WorkingDirectory=root
      WorkingDirectory=/srv/../etc
      WorkingDirectory=..
      WorkingDirectory=%h/work
    """.trimIndent()

    setupFileInEditor("file.nspawn", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(4, highlights)
  }

  @Test
  fun testNamespacePath() {
    // The [Network] key shares config_parse_path; /proc is fine here (no PATH_CHECK_NON_API_VFS).
    // language="unit file (systemd)"
    val file = """
      [Network]
      NamespacePath=/run/netns/foo
      NamespacePath=/proc/1/ns/net
      NamespacePath=netns/foo
      NamespacePath=/run/netns/../foo
    """.trimIndent()

    setupFileInEditor("file.nspawn", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(2, highlights)
  }
}
