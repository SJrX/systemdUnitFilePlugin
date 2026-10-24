package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp4.c#L2060-L2096   config_parse_dhcp_socket_priority
 */

/**
 * Validator for DHCPv4.SocketPriority
 * C Function: config_parse_dhcp_socket_priority(0)
 * Used by Options: DHCPv4.SocketPriority
 *
 * The C implementation reads the value with safe_atoi() and then refuses anything outside
 * 0…TC_PRIO_MAX (15): "Invalid socket priority, must be in 0…15". Empty values are allowed
 * (clears the setting) and so are skipped here.
 */
class ConfigParseDhcpSocketPriorityOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_socket_priority",
    SequenceCombinator(
        SafeAtoTerminal(signed = true, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 15L, acceptsWhitespace = true, acceptsMinus = true),
        EOF()
    )
)
