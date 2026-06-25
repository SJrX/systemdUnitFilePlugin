package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/**
 * Gateway= coverage for systemd .network files (config_parse_route_section, ltypes ROUTE_GATEWAY
 * and ROUTE_GATEWAY_NETWORK). Previously unregistered, so Gateway values were not validated at all.
 *
 * [Network] Gateway= is an IPv4/IPv6 address only; [Route] Gateway= also accepts "_dhcp4"/"_ipv6ra".
 */
class ConfigParseRouteSectionGatewayOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidGateways() {
    // language="unit file (systemd)"
    val file = """
            [Network]
            Gateway=192.168.1.1
            Gateway=2001:db8::1
            [Route]
            Gateway=10.0.0.1
            Gateway=fe80::1
            Gateway=_dhcp4
            Gateway=_ipv6ra
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)

    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidGateways() {
    // [Network] does not accept the special tokens; bad addresses are rejected in both sections;
    // [Route] only accepts _dhcp4 / _ipv6ra (not _dhcp6). One highlight per line.
    // language="unit file (systemd)"
    val file = """
            [Network]
            Gateway=_dhcp4
            Gateway=notanip
            [Route]
            Gateway=_dhcp6
            Gateway=300.1.2.3
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)

    assertSize(4, myFixture.doHighlighting())
  }
}
