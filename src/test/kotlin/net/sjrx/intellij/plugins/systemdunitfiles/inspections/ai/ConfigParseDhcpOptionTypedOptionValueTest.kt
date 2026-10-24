package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcpOptionTypedOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // [DHCPRelay] CircuitId=/RemoteId=/VirtualSubnetSelection= share this parser, but the plugin's
    // section data doesn't know [DHCPRelay] yet; the deprecated [DHCPServer] spellings exercise it.
    // language="unit file (systemd)"
    val file = """
      [DHCPServer]
      RelayAgentCircuitId=string:sample_circuit_id
      RelayAgentRemoteId=string:aabbccdd
      RelayAgentRemoteId=uint8:0xff
      RelayAgentRemoteId=uint16:65535
      RelayAgentRemoteId=uint32:0
      RelayAgentRemoteId=ipv4address:192.0.2.1
      RelayAgentRemoteId=ipv6address:fe80::1
      RelayAgentRemoteId=ipv6address::2001:db8::1
      RelayAgentRemoteId=string:with\x00nul\ttab
      RelayAgentRemoteId=::string:leading-colons
      RelayAgentCircuitId=
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
      RelayAgentCircuitId=sample_circuit_id
      RelayAgentCircuitId=text:hello
      RelayAgentCircuitId=uint8:256
      RelayAgentCircuitId=uint16:-1
      RelayAgentCircuitId=ipv4address:192.0.2.1/24
      RelayAgentCircuitId=string
      RelayAgentRemoteId=ipv6address:::1
      RelayAgentRemoteId=ipv6address::ffff:192.0.2.1
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(8, highlights)
  }
}
