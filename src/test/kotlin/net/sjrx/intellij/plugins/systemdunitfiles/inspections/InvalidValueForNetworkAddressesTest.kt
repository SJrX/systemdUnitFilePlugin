package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueForNetworkAddressesTest : AbstractUnitFileTest() {

  fun testNoWarningWhenValidIPv4NetworkAddressSet() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file="""
      [Network]
      Address=244.178.44.111/32
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenSomeInvalidIPv4NetworkAddressSet() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file="""
      [Network]
      # The first octet is two high
      Address=256.178.44.111
      # Too few octets
      Address=245.124.2/12
      # Too many octets
      Address=1.2.3.4.5/8
      # Invalid Prefix Length
      Address=244.25.2.1/33
      # Invalid Prefix Length
      Address=244.25.2.1/7
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(5, highlights)

  }

  fun testNoWarningWhenValidIPv6NetworkAddressSet() {
    // Fixture Setup

    // language="unit file (systemd)"
    val file="""
      [Network]
      Address=::ffff/64
      Address=::FFFF/64
      Address=::/64
      Address=::1/128
      Address=2001:db8::1/128
      Address=2001:db8:0:0:0:0:2:1/127
      Address=2001:db8:ABCD:0012::0/96
      Address=fe80::/64
      Address=::/128
      Address=::ffff:192.0.2.128/96
      Address=2001:0db8:85a3:0000:0000:8a2e:0370:7334/124
      Address=2001:0db8:85a3::8a2e:0370:7334/124
      Address=fd00:1234:5678:9abc:def0:1234:5678:9abc/100
      Address=2001:db8:0:0:0:0:0:1/126
      Address=2001:db8:85a3::8a2e:370:7334/120
      Address=::ABCD/112
      Address=::1/127
      Address=2001:db8::/65
      Address=ff02::1/128
      # Honestly I don't know what matches this
      Address=2001:0db8:85a3:0000:0000:8a2e:192.168.0.1/96
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenSomeInvalidIPv6NetworkAddressSet() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file="""
      [Network]
    # Too many hextets (9 instead of max 8)
    Address=2001:0db8:85a3:0000:0000:8a2e:0370:7334:1234/64
    # Too few hextets (only 2)
    Address=abcd:1234/64
    # Invalid character (g is not a hex digit)
    Address=2001:db8:85a3:0:0:8a2e:370g:7334/64
    # Double '::' not allowed
    Address=2001:db8::85a3::7334/64
    # Prefix too large (>128)
    Address=2001:db8::1/129
    # Prefix too small (<0)
    Address=2001:db8::1/-1
    # Missing prefix
    Address=2001:db8::1
    # Empty address
    Address=/64
    # Trailing colon
    Address=2001:db8:85a3:0:0:8a2e:370:7334:/64
    # Leading colon (not part of '::')
    Address=:2001:db8:85a3:0:0:8a2e:370:7334/64
    # Too many consecutive colons (illegal ':::')
    Address=2001:db8:::1/64
    # Embedded IPv4 with too many octets
    Address=::ffff:192.168.1.1.1/96
    # Embedded IPv4 with invalid octet (>255)
    Address=::ffff:300.168.1.1/96
    # Hextet too long (more than 4 hex digits)
    Address=2001:db8:12345::1/64
    # Non-hex character in hextet
    Address=2001:db8:zzzz::1/64
    # Invalid number of octets (missing one with no zero compression)
    Address=2001:0db8:85a3:0000:0000:192.168.0.1/96
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(16, highlights)

  }

}
