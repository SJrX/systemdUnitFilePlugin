package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [Address] NetLabel= (ADDRESS_NET_LABEL -> config_parse_string(CONFIG_PARSE_STRING_SAFE)). */
class ConfigParseAddressNetLabelOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [Address]
            NetLabel=system_u:object_r:root_t:s0
            NetLabel=unconfined_u:object_r:*
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
            [Address]
            NetLabel="system_u:object_r:root_t:s0"
            NetLabel=back\\slash
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(2, myFixture.doHighlighting())
  }
}
