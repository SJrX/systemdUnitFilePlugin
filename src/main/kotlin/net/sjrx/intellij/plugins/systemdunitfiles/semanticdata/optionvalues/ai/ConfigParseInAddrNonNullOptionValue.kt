package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for DHCPServer.RelayTarget
 * C Function: config_parse_in_addr_non_null(AF_INET)
 * Used by Options: DHCPServer.RelayTarget
 * 
 * Validates IPv4 addresses in inet_pton(3) format. The C implementation also rejects
 * the ANY address (0.0.0.0) semantically, but syntactically it's a valid IPv4 address.
 */
class ConfigParseInAddrNonNullOptionValue : SimpleGrammarOptionValues(
    "config_parse_in_addr_non_null",
    SequenceCombinator(
        IPV4_ADDR,
        EOF()
    )
)
