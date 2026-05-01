package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for DHCPv4.SocketPriority
 * C Function: config_parse_dhcp_socket_priority(0)
 * Used by Options: DHCPv4.SocketPriority
 *
 * The C implementation uses safe_atoi() with no range restriction beyond what fits in a
 * signed 32-bit int. SO_PRIORITY values 0..6 are typical, but the parser itself accepts
 * any signed int. Empty values are allowed (clears the setting) and so are skipped here.
 */
class ConfigParseDhcpSocketPriorityOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_socket_priority",
    SequenceCombinator(
        IntegerTerminal(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong() + 1L),
        EOF()
    )
)
