package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcp6SendOptionOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [DHCPv6]
      SendOption=1:string:\x41
      SendOption=65534:uint8:1
      SendOption=0x10:uint16:0x1234
      SendOption=23:ipv6address:2001:db8::53
      SendOption=23:ipv4address:10.0.0.1
      SendOption=12:uint32:4294967295
      SendOption=1:ipv6address::2001:db8::1
      SendOption=1:uint8:0b+1
      SendVendorOption=1: 2:uint8:0b 1
      SendVendorOption=123: 1:string:x
      SendVendorOption=123:1:string:hello
      SendVendorOption=0:1:uint8:2
      SendVendorOption=4294967295:65534:string:x
      SendVendorOption=
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // An unknown TYPE (`bytes`, `int8`, `text`) makes systemd drop the WHOLE .network file ("Failed to parse
    // file: Invalid argument"): DHCPOptionDataType has no negative enumerator, so the C's `type < 0` check
    // never fires and the switch's `default: return -EINVAL` does (sd-check, 0f0682b773).
    // language="unit file (systemd)"
    val file = """
      [DHCPv6]
      SendOption=0:string:x
      SendOption=65535:string:x
      SendOption=1:uint8:
      SendOption=1:ipv6address:52:54:00:b9:b5:61
      SendOption=1:string:\U
      SendVendorOption=4294967296:1:string:x
      SendVendorOption=123:1:bytes:x
      SendVendorOption=12:string:x
      SendOption=123:1:string:x
      SendOption=1:ipv6address:::1
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(10, highlights)
  }
}
