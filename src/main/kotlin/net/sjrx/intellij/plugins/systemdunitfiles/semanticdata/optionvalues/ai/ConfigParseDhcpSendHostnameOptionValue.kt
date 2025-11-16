package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for DHCPv4.SendHostname
 * C Function: config_parse_dhcp_send_hostname(AF_INET)
 * Used by Options: DHCPv4.SendHostname
 * 
 * This validator parses boolean values for the SendHostname option.
 * Accepts: yes/no, true/false, on/off, 1/0, y/n, t/f
 */
class ConfigParseDhcpSendHostnameOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_send_hostname",
    SequenceCombinator(
        BOOLEAN,
        EOF()
    )
)
