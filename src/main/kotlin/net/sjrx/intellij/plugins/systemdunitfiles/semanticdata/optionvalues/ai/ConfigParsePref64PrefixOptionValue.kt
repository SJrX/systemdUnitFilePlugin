package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for IPv6PREF64Prefix.Prefix
 * C Function: config_parse_pref64_prefix(0)
 * Used by Options: IPv6PREF64Prefix.Prefix
 * 
 * Validates IPv6 PREF64 (NAT64) prefix configuration.
 * Format: IPv6_address/prefix_length
 * Valid prefix lengths: 96, 64, 56, 48, 40, 32
 */
class ConfigParsePref64PrefixOptionValue : SimpleGrammarOptionValues(
    "config_parse_pref64_prefix",
    SequenceCombinator(
        IPV6_ADDR,
        CIDR_SEPARATOR,
        FlexibleLiteralChoiceTerminal("96", "64", "56", "48", "40", "32"),
        EOF()
    )
)
