package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for IPv6AcceptRA.DHCPv6Client
 * C Function: config_parse_ndisc_start_dhcp6_client(0)
 * Used by Options: IPv6AcceptRA.DHCPv6Client
 * 
 * Accepts boolean values (yes/no/true/false/on/off/1/0/y/n/t/f) or the special value "always".
 */
class ConfigParseNdiscStartDhcp6ClientOptionValue : SimpleGrammarOptionValues(
    "config_parse_ndisc_start_dhcp6_client",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("always", "yes", "no", "true", "false", "on", "off", "1", "0", "y", "n", "t", "f"),
        EOF()
    )
)
