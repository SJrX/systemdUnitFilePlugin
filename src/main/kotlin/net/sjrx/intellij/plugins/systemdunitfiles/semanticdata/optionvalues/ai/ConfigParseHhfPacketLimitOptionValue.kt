package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for HeavyHitterFilter.PacketLimit
 * C Function: config_parse_hhf_packet_limit(QDISC_KIND_HHF)
 * Used by Options: HeavyHitterFilter.PacketLimit
 * 
 * Validates that the value is an unsigned integer in the range 0 to 4294967294.
 * This specifies the hard limit on the queue size in number of packets.
 */
class ConfigParseHhfPacketLimitOptionValue : SimpleGrammarOptionValues(
    "config_parse_hhf_packet_limit",
    SequenceCombinator(
        IntegerTerminal(0, 4294967295),  // Range is 0 to 4294967294 inclusive (max is exclusive)
        EOF()
    )
)
