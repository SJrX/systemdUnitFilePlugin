package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/**
 * RouteTable= in [DHCPv4], [DHCPv6] and [IPv6AcceptRA] (config_parse_dhcp_or_ra_route_table). Every value was checked
 * with systemd's parser at 0f0682b773 on all three keys, which agree. `mytable` and `Main` are rejected there only
 * because sd-check loads no networkd.conf; they are accepted here on purpose (a networkd.conf RouteTable=NAME:NUMBER
 * could define them).
 */
class ConfigParseDhcpOrRaRouteTableOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [DHCPv4]
      RouteTable=211
      RouteTable=main
      RouteTable=default
      RouteTable=local
      RouteTable=4294967295
      RouteTable=0x10
      RouteTable=010
      RouteTable=0b 1
      RouteTable=mytable
      RouteTable=Main
      RouteTable=
      [DHCPv6]
      RouteTable=211
      RouteTable=local
      RouteTable=0x10
      RouteTable=mytable
      [IPv6AcceptRA]
      RouteTable=211
      RouteTable=main
      RouteTable=0b 1
      RouteTable=mytable
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
      [DHCPv4]
      RouteTable=0
      RouteTable=00
      RouteTable=08
      RouteTable=4294967296
      RouteTable=a:b
      RouteTable=1:2
      [DHCPv6]
      RouteTable=0
      RouteTable=4294967296
      RouteTable=a:b
      [IPv6AcceptRA]
      RouteTable=0
      RouteTable=08
      RouteTable=1:2
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(12, highlights)
  }
}
