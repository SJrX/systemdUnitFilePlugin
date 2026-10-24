package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** config_parse_string(CONFIG_PARSE_STRING_SAFE_AND_ASCII). */
class ConfigParseStringSafeAsciiOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [DHCPServer]
            BootFilename=pxelinux.0
            BootFilename=efi/grubx64.efi
            BootFilename=boot file with spaces*.efi
            BootFilename=p@ss:w0rd!#[x]?
            BootFilename=foo*bar[1]?
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValues() {
    // One highlight per line.
    // language="unit file (systemd)"
    val file = """
            [DHCPServer]
            BootFilename=café.efi
            BootFilename=back\\slash
            BootFilename="quoted"
            BootFilename=it's
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(4, myFixture.doHighlighting())
  }
}
