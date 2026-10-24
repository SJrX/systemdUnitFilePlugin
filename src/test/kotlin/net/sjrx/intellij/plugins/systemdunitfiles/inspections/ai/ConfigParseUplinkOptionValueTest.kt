package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUplinkOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // `:none` is only meaningful for [DHCPServer]/[IPv6SendRA] and `:self` only for
    // [DHCPPrefixDelegation]; the validator can't see the section, so both are accepted everywhere.
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
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(6, highlights)
  }
}
