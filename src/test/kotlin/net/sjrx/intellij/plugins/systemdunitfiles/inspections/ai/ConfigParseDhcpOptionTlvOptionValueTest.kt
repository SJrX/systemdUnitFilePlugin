package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcpOptionTlvOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // [DHCPRelay] ExtraOption= shares this parser, but the plugin's section data doesn't know [DHCPRelay]
    // yet, so it can't be exercised here.
    // language="unit file (systemd)"
    val file = """
      [DHCPv4]
      SendOption=12:string:test
      SendOption=1:uint8:255
      SendOption=0x0c:uint16:0xffff
      SendOption=254:uint32:4294967295
      SendOption=014:ipv4address:192.168.1.1
      SendOption=17:ipv6address:2001:db8::1
      SendOption=60:string:has:colons:inside
      SendOption=60:string:tab\there\x41\u00e9\101 é
      SendOption=12::uint8::5
      SendOption=12:uint8: 7
      SendOption=
      SendVendorOption=43:string:vendor
      [DHCPServer]
      SendOption=42:ipv4address:10.0.0.1
      SendVendorOption=43:uint8:1
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
      SendOption=0:string:test
      SendOption=255:string:test
      SendOption=12:uint8:256
      SendOption=12:uint16:65536
      SendOption=12:int8:5
      SendOption=12:ipv4address:300.1.1.1
      SendOption=12:string:
      SendOption=12:string:bad\qescape
      SendOption=12
      SendOption=abc:string:x
      [DHCPServer]
      SendVendorOption=12:ipv6address:1.2.3.4
      SendVendorOption=12:uint32:-1
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(12, highlights)
  }
}
