package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for DHCPv6.WithoutRA
 * C Function: config_parse_dhcp6_client_start_mode(0)
 * Used by Options: DHCPv6.WithoutRA
 * 
 * Validates DHCPv6 client start mode values.
 * Valid values: no, solicit, information-request
 */
class ConfigParseDhcp6ClientStartModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp6_client_start_mode",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("no", "information-request", "solicit"),
        EOF()
    )
)
