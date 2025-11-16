package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for IPv6Prefix.Prefix
 * C Function: config_parse_prefix(0)
 * Used by Options: IPv6Prefix.Prefix
 * 
 * Validates IPv6 prefix in the format: IPv6_address/prefix_length
 * Example: 2001:db8::/32
 */
class ConfigParsePrefixOptionValue : SimpleGrammarOptionValues(
    "config_parse_prefix",
    SequenceCombinator(
        IPV6_ADDR,
        CIDR_SEPARATOR,
        IntegerTerminal(0, 129),  // IPv6 prefix length: 0-128 (129 is exclusive)
        EOF()
    )
)
