package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** config_parse_nft_set(NFT_SET_PARSE_NETWORK) and [Address] NFTSet=. */
class ConfigParseNftSetNetworkOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [Address]
            NFTSet=address:inet:sd_test:addr4 prefix:inet:sd_test:network4 ifindex:inet:sd_test:ifindex
            NFTSet=prefix:netdev:filter:eth_ipv4_prefix
            NFTSet="address:ip6:my.table:set/one"
            [DHCPv4]
            NFTSet=address:ip:filter:dhcp4
            [DHCPv6]
            NFTSet=prefix:ip6:filter:dhcp6_prefix
            [IPv6AcceptRA]
            NFTSet=address:inet:t:s
            [DHCPPrefixDelegation]
            NFTSet=prefix:bridge:t_1:s_2
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValues() {
    // One highlight per line.
    // language="unit file (systemd)"
    val file = """
            [Address]
            NFTSet=cgroup:inet:filter:set
            NFTSet=address:ipx:filter:set
            NFTSet=address:inet:1table:set
            NFTSet=address:inet:table
            NFTSet=address:inet:table:set:extra
            [DHCPv4]
            NFTSet=address:inet:filter:bad-name
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(6, myFixture.doHighlighting())
  }
}
