package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for DHCPServer.RelayAgentRemoteId
 * C Function: config_parse_dhcp_server_relay_agent_suboption(0)
 * Used by Options: DHCPServer.RelayAgentRemoteId
 */
class ConfigParseDhcpServerRelayAgentSuboptionOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_server_relay_agent_suboption",
    SequenceCombinator(
        LiteralChoiceTerminal("string:"),
        RegexTerminal(".*", ".*"),
        EOF()
    )
)
