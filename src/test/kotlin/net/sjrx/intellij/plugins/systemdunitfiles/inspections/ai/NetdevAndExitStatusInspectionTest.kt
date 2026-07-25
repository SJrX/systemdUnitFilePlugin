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
 * Tests for the non-condition validators added in #509: NetDev Kind=, exit-status sets, .link
 * NamePolicy=, WireGuard peer keys, tunnel endpoints and IPv6 address-generation tokens.
 */
class NetdevAndExitStatusInspectionTest : AbstractUnitFileTest() {

  private fun highlights(fileName: String, text: String): Int {
    setupFileInEditor(fileName, text)
    enableInspection(InvalidValueInspection::class.java)
    return myFixture.doHighlighting().size
  }

  private fun assertAccepted(fileName: String, vararg lines: String) =
    assertEquals(lines.joinToString(), 0, highlights(fileName, lines.joinToString("\n") + "\n"))

  private fun assertRejected(fileName: String, text: String) =
    assertTrue(text, highlights(fileName, text) >= 1)

  // ------------------------------------------------------------------ [NetDev] Kind=

  @Test
  fun testNetdevKindAcceptsTableNames() {
    assertAccepted(
      "f.netdev",
      "[NetDev]",
      "Kind=veth", "Kind=bridge", "Kind=wireguard", "Kind=ip6gretap", "Kind=batadv",
      "Kind=bareudp", "Kind=ipoib", "Kind=vxcan", "Kind=xfrm", "Kind=nlmon", "Kind=hsr",
    )
  }

  @Test
  fun testNetdevKindRejectsUnknownAndLists() {
    assertRejected("f.netdev", "[NetDev]\nKind=bogus\n")
    // netdev_kind_from_string takes the whole value; Kind= is not a list.
    assertRejected("f.netdev", "[NetDev]\nKind=gre gretap\n")
  }

  // ------------------------------------------------------------------ exit status sets

  @Test
  fun testExitStatusAcceptsNumbersNamesAndSignals() {
    assertAccepted(
      "f.service",
      "[Service]",
      "SuccessExitStatus=143",
      "SuccessExitStatus=0 143",
      "SuccessExitStatus=137 143",
      "SuccessExitStatus=255",
      "SuccessExitStatus=DATAERR",
      "SuccessExitStatus=DATAERR CANTCREAT",
      "SuccessExitStatus=NOTCONFIGURED",
      "SuccessExitStatus=RUNTIME_DIRECTORY PROTOCOL",
      "SuccessExitStatus=SIGTERM",
      "SuccessExitStatus=TERM",
      "SuccessExitStatus=SIGKILL SIGQUIT 99",
      "RestartPreventExitStatus=1",
      "RestartForceExitStatus=3 4",
    )
  }

  @Test
  fun testExitStatusAcceptsRealtimeSignals() {
    assertAccepted(
      "f.service",
      "[Service]",
      "SuccessExitStatus=RTMIN",
      "SuccessExitStatus=RTMIN+3",
      "SuccessExitStatus=SIGRTMAX",
      "SuccessExitStatus=SIGRTMAX-2",
    )
  }

  @Test
  fun testExitStatusNumbersAcceptEveryBase() {
    // exit_status_from_string() -> safe_atou8() -> strtoul with base 0.
    assertAccepted("f.service", "[Service]", "SuccessExitStatus=0x10", "RestartPreventExitStatus=0377")
    assertRejected("f.service", "[Service]\nSuccessExitStatus=0x100\n")
    assertRejected("f.service", "[Service]\nSuccessExitStatus=0400\n")
  }

  @Test
  fun testExitStatusRejectsOutOfRangeAndUnknownNames() {
    // A bare number is only ever an exit status, so the range is 0…255 (safe_atou8).
    assertRejected("f.service", "[Service]\nSuccessExitStatus=256\n")
    assertRejected("f.service", "[Service]\nSuccessExitStatus=-23\n")
    assertRejected("f.service", "[Service]\nSuccessExitStatus=invalid\n")
    assertRejected("f.service", "[Service]\nSuccessExitStatus=SIGBOGUS\n")
  }

  // ------------------------------------------------------------------ [Link] NamePolicy=

  @Test
  fun testNamePolicyAcceptsTheFullPolicyList() {
    assertAccepted(
      "f.link",
      "[Link]",
      "NamePolicy=keep",
      "NamePolicy=mac",
      "NamePolicy=keep kernel",
      "NamePolicy=keep kernel database onboard slot path",
    )
  }

  @Test
  fun testNamePolicyRejectsUnknownNamesAndCommas() {
    assertRejected("f.link", "[Link]\nNamePolicy=bogus\n")
    assertRejected("f.link", "[Link]\nNamePolicy=keep,kernel\n")
  }

  @Test
  fun testNamePolicyIsNotAlternativeNamesPolicy() {
    // name_policy_table has kernel/keep; alternative_names_policy_table does not.
    assertAccepted("f.link", "[Link]", "NamePolicy=kernel")
    assertRejected("f.link", "[Link]\nAlternativeNamesPolicy=kernel\n")
  }

  // ------------------------------------------------------------------ WireGuard peer keys

  @Test
  fun testWireguardPeerKeyAcceptsBase64AndCredentials() {
    assertAccepted(
      "f.netdev",
      "[WireGuardPeer]",
      "PublicKey=RDf+LSpeEre7YEIKaxg+wbpsNV7du+ktR99uBEtIiCA=",
      "PresharedKey=IIWIV17wutHv7t4cR6pOT91z6NSz/T8Arh0yaywhw3M=",
      "PublicKey=@wg-public-key",
    )
  }

  @Test
  fun testWireguardPeerKeyRejectsWrongLengthAndBadCharacters() {
    // WG_KEY_LEN is 32 bytes, i.e. 43 base64 characters plus optional padding.
    assertRejected("f.netdev", "[WireGuardPeer]\nPublicKey=RDf+LSpeEre7YEIKaxg+wbpsNV7du+ktR99uBEtI=\n")
    assertRejected("f.netdev", "[WireGuardPeer]\nPublicKey=not a key\n")
  }

  // ------------------------------------------------------------------ [Tunnel] Local=/Remote=

  @Test
  fun testTunnelLocalAcceptsAddressesAndLocalAddressTypes() {
    assertAccepted(
      "f.netdev",
      "[Tunnel]",
      "Local=10.65.223.238",
      "Local=2a00:ffde:4567:edde::4987",
      "Local=any",
      "Local=slaac",
      "Local=dhcp4",
      "Local=dhcp6",
      "Local=ipv4_link_local",
      "Local=ipv6_link_local",
      "Local=dhcp_pd",
    )
  }

  @Test
  fun testTunnelRemoteAcceptsAddressesButNotLocalAddressTypes() {
    assertAccepted("f.netdev", "[Tunnel]", "Remote=10.65.223.239", "Remote=2001:473:fece:cafe::5179", "Remote=any")
    // config_parse_tunnel_remote_address has no netdev_local_address_type_from_string() call.
    assertRejected("f.netdev", "[Tunnel]\nRemote=slaac\n")
    assertRejected("f.netdev", "[Tunnel]\nRemote=dhcp4\n")
  }

  @Test
  fun testTunnelAddressesRejectNonsense() {
    // systemd's own 25-vti-tunnel-local-any.netdev sets Local=remote and the test expects the link to
    // come up with "local any", i.e. the value was rejected and the field left unset.
    assertRejected("f.netdev", "[Tunnel]\nLocal=remote\n")
    assertRejected("f.netdev", "[Tunnel]\nLocal=10.65.223\n")
  }

  // ------------------------------------------------------------------ IPv6 address generation

  @Test
  fun testAddressGenerationAcceptsEveryMode() {
    assertAccepted(
      "f.network",
      "[Network]",
      "IPv6Token=eui64",
      "IPv6Token=::1a:2b:3c:4d",
      "IPv6Token=static:::fa:de:ca:fe",
      "IPv6Token=prefixstable",
      "IPv6Token=prefixstable:2002:da8:1::",
      "IPv6Token=prefixstable:2002:da8:1::,86b123b969ba4b7eb8b3d8605123525a",
      "IPv6Token=prefixstable,86b123b969ba4b7eb8b3d8605123525a",
      "IPv6Token=prefixstable,86b123b9-69ba-4b7e-b8b3-d8605123525a",
    )
  }

  @Test
  fun testAddressGenerationRejectsSystemdsOwnNegativeCases() {
    // These four all appear in systemd's 25-ipv6-prefix-veth-token-prefixstable.network as values
    // networkd logs and ignores.
    assertRejected("f.network", "[IPv6AcceptRA]\nToken=prefixstable@\n")
    assertRejected("f.network", "[IPv6AcceptRA]\nToken=prefixstable,\n")
    assertRejected("f.network", "[IPv6AcceptRA]\nToken=prefixstable,00000000000000000000000000000000\n")
    assertRejected("f.network", "[IPv6AcceptRA]\nToken=static\n")
    assertRejected("f.network", "[IPv6AcceptRA]\nToken=static:\n")
  }
}
