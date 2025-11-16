package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for DHCPServer.PersistLeases
 * C Function: config_parse_dhcp_server_persist_leases(0)
 * Used by Options: DHCPServer.PersistLeases
 */
class ConfigParseDhcpServerPersistLeasesOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_server_persist_leases",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("runtime", "1", "yes", "y", "true", "t", "on", "0", "no", "n", "false", "f", "off"),
        EOF()
    )
)
