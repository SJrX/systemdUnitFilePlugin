package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.DHCP
 * C Function: config_parse_dhcp(0)
 * Used by Options: Network.DHCP
 * 
 * Enables DHCPv4 and/or DHCPv6 client support.
 * Accepts: yes, no, ipv4, or ipv6
 */
class ConfigParseDhcpOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("yes", "no", "ipv4", "ipv6"),
        EOF()
    )
)
