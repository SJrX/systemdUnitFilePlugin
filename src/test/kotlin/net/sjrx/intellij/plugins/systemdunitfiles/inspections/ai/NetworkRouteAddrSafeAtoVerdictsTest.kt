package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseIpv6AddressLabelOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseIpv6AddressLabelPrefixOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseNdiscRouteMetricOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseRouteMetricInitCwndOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseRouteMultiPathOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseRoutingPolicyRuleFirewallMarkOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseRoutingPolicyRuleIpProtocolOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseRoutingPolicyRuleUidRangeOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseStackedNetdevOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarEngineVerdict
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarOptionValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Values containing a vertical tab (`\v`), which config_parse() does not strip (it only strips " \t\n\r") and
 * which strtoul() skips. They are hard to write in a fixture file, so they are checked here on the grammar, for
 * both engines. Every verdict was confirmed with steve-local/systemd-tools/bin/sd-check at 0f0682b773, one value
 * per .network file.
 */
class NetworkRouteAddrSafeAtoVerdictsTest {

  private fun accepted(optionValue: GrammarOptionValue, value: String) {
    assertEquals("classic engine on <$value>", GrammarEngineVerdict.VALID, GrammarEngineVerdict.classic(optionValue.combinator, value))
    assertEquals("parse engine on <$value>", GrammarEngineVerdict.VALID, GrammarEngineVerdict.parseEngine(optionValue.combinator, value))
  }

  private fun rejected(optionValue: GrammarOptionValue, value: String) {
    assertNotEquals("classic engine on <$value>", GrammarEngineVerdict.VALID, GrammarEngineVerdict.classic(optionValue.combinator, value))
    assertNotEquals("parse engine on <$value>", GrammarEngineVerdict.VALID, GrammarEngineVerdict.parseEngine(optionValue.combinator, value))
  }

  @Test
  fun testLeadingVerticalTabReachesSafeAtou() {
    accepted(ConfigParseRouteMetricInitCwndOptionValue(), "\u000B5")
    accepted(ConfigParseIpv6AddressLabelOptionValue(), "\u000B5")
    accepted(ConfigParseRoutingPolicyRuleFirewallMarkOptionValue(), "\u000B5")
    accepted(ConfigParseRoutingPolicyRuleFirewallMarkOptionValue(), "1/\u000B5")
    accepted(ConfigParseNdiscRouteMetricOptionValue(), "\u000B5")
    accepted(ConfigParseRouteMultiPathOptionValue(), "@eth0 \u000B5")
    accepted(ConfigParseIpv6AddressLabelPrefixOptionValue(), "::/\u000B64")
    // safe_atoi, through parse_ip_protocol_full: 6 is tcp.
    accepted(ConfigParseRoutingPolicyRuleIpProtocolOptionValue(), "\u000B6")
  }

  @Test
  fun testParseUidLetsVerticalTabThrough() {
    // SAFE_ATO_REFUSE_LEADING_WHITESPACE only refuses " \t\n\r"; strtoul then skips the \v.
    accepted(ConfigParseRoutingPolicyRuleUidRangeOptionValue(), "\u000B5")
    accepted(ConfigParseRoutingPolicyRuleUidRangeOptionValue(), "5-\u000B6")
    // A space is refused by the same flag (sd-check: "Invalid uid or uid range '5- 6'").
    rejected(ConfigParseRoutingPolicyRuleUidRangeOptionValue(), "5- 6")
  }

  @Test
  fun testIfindexWithVerticalTabIsNotAName() {
    // parse_ifindex("\v5") is 5, so ifname_valid() refuses it (sd-check: "Invalid netdev name in VLAN=").
    rejected(ConfigParseStackedNetdevOptionValue(), "\u000B5")
  }
}
