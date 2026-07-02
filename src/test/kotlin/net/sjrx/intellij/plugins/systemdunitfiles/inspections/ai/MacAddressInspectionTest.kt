package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/**
 * End-to-end (default/classic engine) wiring tests for the MAC / hardware-address validators (#501):
 * each directive resolves to its grammar validator, accepts a well-formed value, and flags a bad one.
 * The grammar itself is covered in depth by MacAddressGrammarTest; this proves the section/key -> parser
 * mapping and that the grammar also matches under the original SyntacticMatch/SemanticMatch path.
 */
class MacAddressInspectionTest : AbstractUnitFileTest() {

  private fun highlights(fileName: String, text: String): Int {
    setupFileInEditor(fileName, text)
    enableInspection(InvalidValueInspection::class.java)
    return myFixture.doHighlighting().size
  }

  // A well-formed value must produce no InvalidValue highlights.
  private fun assertAccepted(fileName: String, text: String) = assertEquals(0, highlights(fileName, text))

  // A malformed value must be flagged. The classic path can report a partial match as more than one
  // region, so we only require "at least one" here; MacAddressGrammarTest pins the new-engine outcome.
  private fun assertRejected(fileName: String, text: String) = assertTrue(highlights(fileName, text) >= 1)

  @Test
  fun testMatchMacAddressListAcceptsMacsAndIpLiterals() {
    // config_parse_hw_addrs: whitespace-separated hardware addresses, IP literals allowed.
    assertAccepted("f.network", "[Match]\nMACAddress=00:11:22:33:44:55 aa-bb-cc-dd-ee-ff 192.168.1.1\n")
    assertAccepted("f.network", "[Match]\nPermanentMACAddress=0011.2233.4455\n")
    assertRejected("f.network", "[Match]\nMACAddress=zz:zz:zz:zz:zz:zz\n")
  }

  @Test
  fun testLinkMacAddressAcceptsIpLiteralButNotBadLength() {
    // config_parse_hw_addr (expected_len == 0): single hardware address or IP literal.
    assertAccepted("f.network", "[Link]\nMACAddress=2001:db8::1\n")
    assertAccepted("f.link", "[Link]\nMACAddress=00:11:22:33:44:55\n")
    assertRejected("f.network", "[Link]\nMACAddress=00:11:22\n")
  }

  @Test
  fun testNeighborLinkLayerAddress() {
    // config_parse_neighbor_section / NEIGHBOR_LINK_LAYER_ADDRESS -> hardware address or IP literal.
    assertAccepted("f.network", "[Neighbor]\nLinkLayerAddress=192.168.1.1\n")
    assertRejected("f.network", "[Neighbor]\nLinkLayerAddress=nonsense\n")
  }

  @Test
  fun testSrIovMacRejectsIpLiteral() {
    // config_parse_sr_iov_mac -> strict 6-byte MAC; an IP literal is invalid here.
    assertAccepted("f.link", "[SR-IOV]\nMACAddress=00:11:22:33:44:55\n")
    assertRejected("f.network", "[SR-IOV]\nMACAddress=192.168.1.1\n")
  }

  @Test
  fun testBridgeFdbAndDhcpStaticLeaseMac() {
    assertAccepted("f.network", "[BridgeFDB]\nMACAddress=00-11-22-33-44-55\n")
    assertRejected("f.network", "[BridgeFDB]\nMACAddress=00:11:22:33\n")
    assertAccepted("f.network", "[DHCPServerStaticLease]\nMACAddress=de:ad:be:ef:00:01\n")
  }

  @Test
  fun testNetdevPeerAndMacsecMac() {
    // config_parse_netdev_hw_addr (ETH_ALEN) and config_parse_macsec_hw_address -> strict 6-byte MAC.
    assertAccepted("f.netdev", "[NetDev]\nMACAddress=00:11:22:33:44:55\n")
    assertRejected("f.netdev", "[NetDev]\nMACAddress=192.168.1.1\n")
    assertAccepted("f.netdev", "[MACsecReceiveChannel]\nMACAddress=0011.2233.4455\n")
  }

  @Test
  fun testMacvlanSourceMacAddressList() {
    // config_parse_ether_addrs: whitespace-separated list of strict 6-byte MACs (no IP literals).
    assertAccepted("f.netdev", "[MACVLAN]\nSourceMACAddress=00:11:22:33:44:55 aa:bb:cc:dd:ee:ff\n")
    assertRejected("f.netdev", "[MACVLAN]\nSourceMACAddress=00:11:22:33:44:55 192.168.1.1\n")
  }
}