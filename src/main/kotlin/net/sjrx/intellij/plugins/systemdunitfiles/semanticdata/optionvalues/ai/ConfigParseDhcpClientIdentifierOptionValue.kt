package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for DHCPv4.ClientIdentifier
 * C Function: config_parse_dhcp_client_identifier(0)
 * Used by Options: DHCPv4.ClientIdentifier
 */
class ConfigParseDhcpClientIdentifierOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_client_identifier",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("mac", "duid"),
        EOF()
    )
)
