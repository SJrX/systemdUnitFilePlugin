package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for DHCPv6.PrefixDelegationHint
 * C Function: config_parse_dhcp6_pd_prefix_hint(0)
 * Used by Options: DHCPv6.PrefixDelegationHint
 * 
 * Validates IPv6 addresses with prefix length in the format: 2001:db8::/64
 * Prefix length must be in the range 1-128 (inclusive).
 */
class ConfigParseDhcp6PdPrefixHintOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp6_pd_prefix_hint",
    SequenceCombinator(
        IPV6_ADDR,
        CIDR_SEPARATOR,
        IntegerTerminal(1, 129),  // 1-128 inclusive (max is exclusive)
        EOF()
    )
)
