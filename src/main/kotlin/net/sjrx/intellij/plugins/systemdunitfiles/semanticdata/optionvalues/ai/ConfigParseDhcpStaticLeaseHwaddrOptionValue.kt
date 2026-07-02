package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for [DHCPServerStaticLease] MACAddress= (.network).
 * C Function: config_parse_dhcp_static_lease_hwaddr -> parse_ether_addr (a single 6-byte Ethernet MAC).
 */
class ConfigParseDhcpStaticLeaseHwaddrOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_static_lease_hwaddr",
    SequenceCombinator(MAC_ADDRESS, EOF())
)
