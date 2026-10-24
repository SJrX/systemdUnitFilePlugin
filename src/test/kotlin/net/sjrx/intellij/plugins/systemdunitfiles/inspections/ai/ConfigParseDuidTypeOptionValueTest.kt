package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDuidTypeOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // [DHCPv6] uses config_parse_duid_type and [DHCPv4] config_parse_network_duid_type; same grammar.
    // Values from systemd's test-networkd-conf.c and test-network fixtures.
    // language="unit file (systemd)"
    val file = """
      [DHCPv6]
      DUIDType=link-layer-time
      DUIDType=link-layer-time:2000-01-01 00:00:00 UTC
      DUIDType=vendor
      DUIDType=link-layer
      DUIDType=uuid
      DUIDType=0x42
      DUIDType=1:2000-01-01 00:00:00 UTC
      DUIDType=vendor:
      [DHCPv4]
      DUIDType=uuid
      DUIDType=link-layer-time:@1700000000
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [DHCPv6]
      DUIDType=foo
      DUIDType=vendor:2000-01-01 00:00:00 UTC
      DUIDType=uuid:2000-01-01 00:00:00 UTC
      DUIDType=65536
      [DHCPv4]
      DUIDType=Vendor
      DUIDType=2:2000-01-01
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(6, highlights)
  }
}
