package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Tunnel.IPv6RapidDeploymentPrefix
 * C Function: config_parse_6rd_prefix(0)
 * Used by Options: Tunnel.IPv6RapidDeploymentPrefix
 * 
 * Validates IPv6 addresses with a non-zero prefix length for 6rd (IPv6 Rapid Deployment).
 * Format: IPv6_address/prefix_length where prefix_length must be 1-128.
 */
class ConfigParse6rdPrefixOptionValue : SimpleGrammarOptionValues(
    "config_parse_6rd_prefix",
    SequenceCombinator(
        IPV6_ADDR,
        CIDR_SEPARATOR,
        IntegerTerminal(1, 129),  // Non-zero prefix length: 1-128 inclusive (129 is exclusive upper bound)
        EOF()
    )
)
