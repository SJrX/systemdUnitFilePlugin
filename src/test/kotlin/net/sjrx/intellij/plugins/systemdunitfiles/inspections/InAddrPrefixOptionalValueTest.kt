package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InAddrPrefixOptionalValueTest : AbstractUnitFileTest() {

  fun testNoWarningWhenValidIPAddressesInServiceSet() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file="""
      [Service]
      IPAddressAllow=244.178.44.111/32
      IPAddressAllow=any
      IPAddressAllow=localhost
      IPAddressAllow=link-local
      IPAddressAllow=multicast
      IPAddressAllow=127.0.0.1
      IPAddressAllow=1.2.3.4 5.6.7.8/23
      IPAddressAllow=::FFFF/64
      IPAddressAllow=::/64
      IPAddressAllow=::1/128
      IPAddressAllow=2001:db8::1/128        2001:db8:ABCD:0012::0/96 2001:0db8:85a3:0000:0000:8a2e:0370:7334 multicast 
      IPAddressAllow=fe80::/64
      IPAddressAllow=::/128
      IPAddressAllow=::ffff:192.0.2.128/96
      IPAddressAllow=2001:0db8:85a3::8a2e:0370:7334/124
      IPAddressDeny=any
      IPAddressDeny=localhost
      IPAddressDeny=link-local
      IPAddressDeny=multicast
      IPAddressDeny=64.2.3.4 1.2.3.5 6.9.0.1
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningsWhenInvalidIPAddressesInServiceSet() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file="""
      [Service]
      # Not a valid option
      IPAddressAllow=all
      # First octet is too high
      IPAddressAllow=256.178.44.111
      # Too few octets
      IPAddressAllow=245.124.2/12
      # Too many octets
      IPAddressDeny=1.2.3.4.5/8
      # Invalid Prefix Length x 2 but valid IPs in between
      IPAddressAllow=244.25.2.1/33 4.2.3.5 244.25.2.1/7 any      
      IPAddressDeny=2001:0db8:85a3:0000:0000:8a2e:0370:7334:1234/64
      # Too few hextets (only 2)
      # Invalid character (g is not a hex digit)
      IPAddressDeny=abcd:1234/64 2001:db8:85a3:0:0:8a2e:370g:7334/64 2001:db8::85a3::7334/64
      # Prefix too large (>128)
      IPAddressDeny=2001:db8::1/129
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(8, highlights)
  }

  fun testNoWarningWhenValidIPv4AndIPv6AddressesSetInNetwork() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file="""
      [DHCPv4]
      AllowList=1.2.3.4/32
      AllowList=any
      
      [IPv6AcceptRA]
      RouterAllowList=2001:0db8:85a3::8a2e:0370:7334/124
      RouterAllowList=link-local
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenValidIPv4AndIPv6AddressesSetInNetwork() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file="""
      [DHCPv4]
      AllowList=2001:0db8:85a3::8a2e:0370:7334/124
      AllowList=any
      
      [IPv6AcceptRA]
      RouterAllowList=1.2.3.4
      RouterAllowList=link-local
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(2, highlights)
  }



}
