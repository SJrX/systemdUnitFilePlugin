package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/*
 * Expectations here are derived from systemd's C parsers at a8e93919c3 (https://github.com/systemd/systemd/blob/a8e93919c3),
 * the commit systemd-build/build/last_commit_hash pins, and NOT from what happens to appear in
 * real-world unit files. Where a case is subtle the individual test says which routine decides it.
 *
 * Several of the rejection cases are lifted from systemd's own negative fixtures under
 * test/test-network/conf/ and from KDE's syntax-highlighting test input, both of which deliberately
 * contain malformed values.
 */

/**
 * Tests for the `[RoutingPolicyRule]`, `[Route]`, `[Address]` and `[NextHop]` validators added in
 * #509, plus the assorted networkd settings that came with them.
 *
 * The accepted values are drawn from systemd's own test-network fixtures where possible, and the
 * rejected ones from what its parsers actually refuse.
 */
class NetworkSectionInspectionTest : AbstractUnitFileTest() {

  private fun highlights(fileName: String, text: String): Int {
    setupFileInEditor(fileName, text)
    enableInspection(InvalidValueInspection::class.java)
    return myFixture.doHighlighting().size
  }

  private fun assertAccepted(fileName: String, vararg lines: String) =
    assertEquals(lines.joinToString(), 0, highlights(fileName, lines.joinToString("\n") + "\n"))

  private fun assertRejected(fileName: String, text: String) =
    assertTrue(text, highlights(fileName, text) >= 1)

  // ------------------------------------------------------------------ [RoutingPolicyRule]

  @Test
  fun testRuleAddressesAndInterfaces() {
    assertAccepted(
      "f.network",
      "[RoutingPolicyRule]",
      "From=192.168.100.18",
      "From=0.0.0.0/8",
      "To=2000:f00::227",
      "To=::/0",
      "IncomingInterface=test1",
      "OutgoingInterface=dummy98",
    )
    assertRejected("f.network", "[RoutingPolicyRule]\nFrom=192.168.0.300\n")
    assertRejected("f.network", "[RoutingPolicyRule]\nTo=192.168.0.1/33\n")
    // ifname_valid() refuses a name that is entirely digits, so it can't be mistaken for an ifindex.
    assertRejected("f.network", "[RoutingPolicyRule]\nIncomingInterface=12345\n")
    // ...and refuses "all"/"default", which collide with /proc/sys/net/*/conf/.
    assertRejected("f.network", "[RoutingPolicyRule]\nIncomingInterface=default\n")
    // IFNAMSIZ - 1 is 15 characters.
    assertRejected("f.network", "[RoutingPolicyRule]\nIncomingInterface=abcdefghijklmnopq\n")
  }

  @Test
  fun testRuleNumbersAcceptEveryBaseSafeAtouReads() {
    assertAccepted(
      "f.network",
      "[RoutingPolicyRule]",
      "Priority=111",
      "Priority=4294967295",
      // safe_atou* pass base 0 to strtoul, so hex and octal are legal; this spelling comes straight
      // out of systemd's 25-fibrule-uidrange.network.
      "TypeOfService=0x08",
      "TypeOfService=255",
      "SuppressPrefixLength=128",
      "SuppressInterfaceGroup=42",
      "GoTo=111",
    )
    assertRejected("f.network", "[RoutingPolicyRule]\nPriority=4294967296\n")
    assertRejected("f.network", "[RoutingPolicyRule]\nTypeOfService=256\n")
    assertRejected("f.network", "[RoutingPolicyRule]\nSuppressPrefixLength=129\n")
    assertRejected("f.network", "[RoutingPolicyRule]\nGoTo=0\n")
    assertRejected("f.network", "[RoutingPolicyRule]\nPriority=abc\n")
  }

  @Test
  fun testRuleEnumsAndPorts() {
    assertAccepted(
      "f.network",
      "[RoutingPolicyRule]",
      "Family=both", "Family=ipv4", "Family=ipv6",
      "Type=blackhole", "Type=unreachable", "Type=prohibit", "Type=goto", "Type=nop", "Type=table",
      "SourcePort=1123", "DestinationPort=3456-3458",
      "Invert=yes", "L3MasterDevice=no",
    )
    // The rule table spells the any-family value "both", not "yes".
    assertRejected("f.network", "[RoutingPolicyRule]\nFamily=yes\n")
    assertRejected("f.network", "[RoutingPolicyRule]\nType=bogus\n")
    // parse_ip_port_range is called with allow_zero = false.
    assertRejected("f.network", "[RoutingPolicyRule]\nSourcePort=0\n")
    assertRejected("f.network", "[RoutingPolicyRule]\nDestinationPort=70000\n")
  }

  // ------------------------------------------------------------------ [Route]

  @Test
  fun testRouteSettings() {
    assertAccepted(
      "f.network",
      "[Route]",
      "Destination=149.10.124.64",
      "Destination=2001:1234:5:8fff:ff:ff:ff:ff/128",
      "Source=192.168.1.1",
      "PreferredSource=10.10.10.1",
      "PreferredSource=2001:1234:56:8f63::2",
      // config_parse_preferred_src accepts a boolean only when it parses as false.
      "PreferredSource=no",
      "GatewayOnLink=yes",
      "Scope=link", "Scope=global", "Scope=host", "Scope=site", "Scope=nowhere", "Scope=200",
    )
    assertRejected("f.network", "[Route]\nDestination=nonsense\n")
    assertRejected("f.network", "[Route]\nPreferredSource=yes\n")
    assertRejected("f.network", "[Route]\nScope=256\n")
    assertRejected("f.network", "[Route]\nScope=bogus\n")
  }

  // ------------------------------------------------------------------ [Address]

  @Test
  fun testAddressSettings() {
    assertAccepted(
      "f.network",
      "[Address]",
      "Peer=192.168.30.1/32",
      "Peer=2001:db8:0:f103::101/128",
      "Broadcast=192.168.1.255",
      "Broadcast=yes",
      "PreferredLifetime=forever",
      "PreferredLifetime=infinity",
      "PreferredLifetime=0",
    )
    assertRejected("f.network", "[Address]\nPeer=hoge\n")
    // config_parse_broadcast delegates with ltype AF_INET, so an IPv6 literal is refused.
    assertRejected("f.network", "[Address]\nBroadcast=::1\n")
    // Not a general time span — the parser only takes forever/infinity/0.
    assertRejected("f.network", "[Address]\nPreferredLifetime=10s\n")
  }

  @Test
  fun testAddressFlagsAreBooleansNotNumbers() {
    // Regression: these five shared the uint32 grammar written for RouteMetric=, so every real
    // value was flagged.
    assertAccepted(
      "f.network",
      "[Address]",
      "AddPrefixRoute=no",
      "HomeAddress=yes",
      "ManageTemporaryAddress=yes",
      "PrefixRoute=false",
      "AutoJoin=yes",
      "DuplicateAddressDetection=ipv4",
      "DuplicateAddressDetection=both",
      "DuplicateAddressDetection=none",
      // config_parse_address_dad tries parse_boolean() first and accepts it with a warning, so these
      // are legal (if confusing) rather than invalid.
      "DuplicateAddressDetection=yes",
      "DuplicateAddressDetection=no",
      "DuplicateAddressDetection=0",
      "RouteMetric=128",
    )
    assertRejected("f.network", "[Address]\nAddPrefixRoute=bogus\n")
    // Same prefix hazard as ConditionVirtualization: the deprecated boolean must not eat the "no"
    // out of "none".
    assertAccepted("f.network", "[Address]", "DuplicateAddressDetection=none", "DuplicateAddressDetection=n")
    assertRejected("f.network", "[Address]\nRouteMetric=hoge\n")
  }

  // ------------------------------------------------------------------ [NextHop]

  @Test
  fun testNextHopSettings() {
    // Regression: Id=, Family= and Group= shared the boolean grammar written for OnLink=.
    assertAccepted(
      "f.network",
      "[NextHop]",
      "Id=20",
      "Gateway=192.168.5.1",
      "Gateway=2001:1234:5:8f63::2",
      "Family=ipv4",
      "Family=ipv6",
      "Group=1:3 20:1",
      "Group=5",
      "OnLink=yes",
      "Blackhole=no",
    )
    assertRejected("f.network", "[NextHop]\nId=nope\n")
    // nexthop_address_family_table has no "both".
    assertRejected("f.network", "[NextHop]\nFamily=both\n")
    assertRejected("f.network", "[NextHop]\nGateway=192.168.5.1/24\n")
    // A group weight must be 1…256.
    assertRejected("f.network", "[NextHop]\nGroup=1:0\n")
    assertRejected("f.network", "[NextHop]\nGroup=1:257\n")
  }

  // ------------------------------------------------------------------ assorted networkd settings

  @Test
  fun testMatchInterfaceNames() {
    assertAccepted(
      "f.network",
      "[Match]",
      "Name=dummy98",
      "Name=veth-peer host0",
      "Name=!loopback",
      "Name=ve-* ns-*",
    )
    assertAccepted("f.link", "[Match]", "OriginalName=*", "OriginalName=test1")
    assertRejected("f.network", "[Match]\nName=eth:0\n")
    assertRejected("f.network", "[Match]\nName=eth/0\n")
    assertRejected("f.network", "[Match]\nName=99\n")
    // .link OriginalName= drops IFNAME_VALID_ALTERNATIVE, so 15 characters is the cap there.
    assertAccepted("f.network", "[Match]", "Name=abcdefghijklmnopqrstuvwxyz")
    assertRejected("f.link", "[Match]\nOriginalName=abcdefghijklmnopqrstuvwxyz\n")
  }

  @Test
  fun testVlanRangesTunnelKeysAndLifetimes() {
    assertAccepted(
      "f.network",
      "[BridgeVLAN]",
      "VLAN=1018-1023",
      "VLAN=100",
      "EgressUntagged=1200-1210",
      "PVID=560",
    )
    assertRejected("f.network", "[BridgeVLAN]\nVLAN=4095\n")
    assertRejected("f.network", "[BridgeVLAN]\nVLAN=1-\n")

    assertAccepted("f.netdev", "[Tunnel]", "Key=101", "InputKey=1.2.3.103", "OutputKey=4294967295")
    assertRejected("f.netdev", "[Tunnel]\nKey=4294967296\n")
    assertRejected("f.netdev", "[Tunnel]\nKey=nope\n")

    assertAccepted("f.network", "[IPv6Prefix]", "PreferredLifetimeSec=1000s", "ValidLifetimeSec=2100s")
    assertRejected("f.network", "[IPv6Prefix]\nValidLifetimeSec=soon\n")
  }

  @Test
  fun testMatchIfnamesAllowsSpaceAfterTheInversionMarker() {
    // `invert = *p == '!'; p += invert;` and then extract_first_word, which skips leading whitespace.
    assertAccepted("f.network", "[Match]", "Name=! eth0", "Name=!  veth-peer host0")
    assertAccepted("f.link", "[Match]", "OriginalName=! enp0s1")
  }

  @Test
  fun testPeerFollowsTheCParserNotTheStricterAddressGrammar() {
    // config_parse_in_addr_prefix retries without a prefix and allows the whole 0…128 range.
    assertAccepted(
      "f.network",
      "[Address]",
      "Peer=2001:db8::2",
      "Peer=fd00::2/56",
      "Peer=10.0.0.2",
      "Peer=10.0.0.2/4",
    )
  }

  @Test
  fun testNumbersAcceptEveryBaseAndStillRangeCheck() {
    // safe_atou* run strtoul with base 0, so hex and octal are legal spellings...
    assertAccepted(
      "f.netdev",
      "[Tunnel]",
      "Key=0x11223344",
      "InputKey=0755",
    )
    assertAccepted("f.network", "[BridgeVLAN]", "VLAN=0x10", "EgressUntagged=0777")
    // ...and the range check has to survive the change of base.
    assertRejected("f.network", "[RoutingPolicyRule]\nTypeOfService=0x1FF\n")
    assertRejected("f.network", "[RoutingPolicyRule]\nTypeOfService=0400\n")
    assertRejected("f.network", "[RoutingPolicyRule]\nGoTo=00\n")
    assertRejected("f.network", "[BridgeVLAN]\nVLAN=0xFFF\n")
    assertRejected("f.network", "[NextHop]\nGroup=5:0401\n")
  }

  @Test
  fun testMtuBounds() {
    assertAccepted("f.netdev", "[NetDev]", "MTUBytes=1480", "MTUBytes=9000", "MTUBytes=16")
    assertAccepted("f.network", "[Network]", "IPv6MTUBytes=1500")
    assertAccepted("f.network", "[DHCPv4]", "RouteMTUBytes=1500")
    // IPV6_MIN_MTU is 1280 and IPV4_MIN_MTU is 68; AF_UNSPEC has no minimum.
    assertRejected("f.network", "[Network]\nIPv6MTUBytes=1000\n")
    assertRejected("f.network", "[DHCPv4]\nRouteMTUBytes=32\n")
    assertRejected("f.netdev", "[NetDev]\nMTUBytes=huge\n")
    // parse_size() bounds the value, not the spelling: 1K is 1024 bytes, under IPV6_MIN_MTU.
    assertAccepted("f.network", "[Network]", "IPv6MTUBytes=2K")
    assertRejected("f.network", "[Network]\nIPv6MTUBytes=1K\n")
    assertRejected("f.network", "[DHCPv4]\nRouteMTUBytes=8B\n")
    assertRejected("f.netdev", "[NetDev]\nMTUBytes=8E\n")
  }
}
