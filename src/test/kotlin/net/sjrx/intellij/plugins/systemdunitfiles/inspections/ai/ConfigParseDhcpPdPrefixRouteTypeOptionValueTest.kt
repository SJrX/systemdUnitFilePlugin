package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcpPdPrefixRouteTypeOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [DHCPv4]
      UnassignedSubnetPolicy=none
      UnassignedSubnetPolicy=unreachable
      UnassignedSubnetPolicy=
      [DHCPv6]
      UnassignedSubnetPolicy=blackhole
      UnassignedSubnetPolicy=prohibit
      UnassignedSubnetPolicy=throw
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
      UnassignedSubnetPolicy=unicast
      UnassignedSubnetPolicy=no
      [DHCPv6]
      UnassignedSubnetPolicy=local
      UnassignedSubnetPolicy=Blackhole
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(4, highlights)
  }
}
