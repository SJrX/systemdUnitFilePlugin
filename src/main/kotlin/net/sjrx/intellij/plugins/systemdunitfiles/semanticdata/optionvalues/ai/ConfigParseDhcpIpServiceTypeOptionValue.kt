package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for DHCPv4.IPServiceType
 * C Function: config_parse_dhcp_ip_service_type(0)
 * Used by Options: DHCPv4.IPServiceType
 * 
 * Validates IP service type values for DHCP packets.
 * Valid values are: none, CS6, CS4
 */
class ConfigParseDhcpIpServiceTypeOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_ip_service_type",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("none", "CS6", "CS4"),
        EOF()
    )
)
