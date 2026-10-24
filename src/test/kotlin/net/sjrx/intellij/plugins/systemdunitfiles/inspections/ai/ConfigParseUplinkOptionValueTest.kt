package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUplinkOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // `:none` is only accepted in [DHCPServer]/[IPv6SendRA] and `:self` only in [DHCPPrefixDelegation].
    // language="unit file (systemd)"
    val file = """
      [DHCPServer]
      UplinkInterface=:auto
      UplinkInterface=:none
      UplinkInterface=dummy98
      UplinkInterface=
      [DHCPPrefixDelegation]
      UplinkInterface=:self
      UplinkInterface=auto
      UplinkInterface=veth99
      UplinkInterface=12
      UplinkInterface=1eth
      [IPv6SendRA]
      UplinkInterface=this-is-a-long-alternative-interface-name
      UplinkInterface=0x10
      UplinkInterface=:none
      UplinkInterface=010
      UplinkInterface=2147483647
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
      [DHCPServer]
      UplinkInterface=:bogus
      UplinkInterface=0
      UplinkInterface=eth0:1
      [DHCPPrefixDelegation]
      UplinkInterface=all
      UplinkInterface=eth/0
      [IPv6SendRA]
      UplinkInterface=eth0 eth1
      UplinkInterface=:self
      UplinkInterface=99999999999
      UplinkInterface=2147483648
      UplinkInterface=08
      [DHCPServer]
      UplinkInterface=:self
      [DHCPPrefixDelegation]
      UplinkInterface=:none
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(12, highlights)
  }
}
