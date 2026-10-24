package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseTemporaryFilesystemsOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      TemporaryFileSystem=/var:ro
      TemporaryFileSystem=/var:ro,mode=0700,nostrictatime
      TemporaryFileSystem=/tmp:ro /usr:ro
      TemporaryFileSystem=/var
      TemporaryFileSystem=/var:
      TemporaryFileSystem=%t/foo:size=10%
      TemporaryFileSystem=/run/a\ b:ro
      TemporaryFileSystem="/run/a b":ro
      TemporaryFileSystem=/run/x:context=system_u:object_r:tmp_t:s0
      TemporaryFileSystem=
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      TemporaryFileSystem=var:ro
      TemporaryFileSystem=:ro
      TemporaryFileSystem=/var/../etc
      TemporaryFileSystem=/tmp relative
      TemporaryFileSystem="/unbalanced
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(5, highlights)
  }
}
