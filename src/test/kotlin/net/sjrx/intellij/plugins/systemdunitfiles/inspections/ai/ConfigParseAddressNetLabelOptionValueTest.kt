package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [Address] NetLabel= (ADDRESS_NET_LABEL -> config_parse_string(CONFIG_PARSE_STRING_SAFE)). */
class ConfigParseAddressNetLabelOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // CONFIG_PARSE_STRING_SAFE uses STRING_ALLOW_GLOBS and does not require ASCII, so globs and UTF-8 are fine
    // (systemd oracle check 2026-10-24).
    // language="unit file (systemd)"
    val file = """
            [Address]
            NetLabel=system_u:object_r:root_t:s0
            NetLabel=unconfined_u:object_r:*
            NetLabel=foo*?
            NetLabel=café
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValues() {
    // systemd logs these at warning level, but it drops the value and with it the whole [Address] section
    // (config_section_parse returns 0, so address_unref_or_set_invalidp marks the section invalid).
    // One highlight per line.
    // language="unit file (systemd)"
    val file = """
            [Address]
            NetLabel="system_u:object_r:root_t:s0"
            NetLabel=back\\slash
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(2, myFixture.doHighlighting())
  }
}
