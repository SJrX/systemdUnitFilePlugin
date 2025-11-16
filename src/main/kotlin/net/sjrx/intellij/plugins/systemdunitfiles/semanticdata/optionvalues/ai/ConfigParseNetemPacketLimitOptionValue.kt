package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for NetworkEmulator.PacketLimit
 * C Function: config_parse_netem_packet_limit(QDISC_KIND_NETEM)
 * Used by Options: NetworkEmulator.PacketLimit
 * 
 * Validates an unsigned integer in the range 0…4294967294 representing
 * the maximum number of packets the qdisc may hold queued at a time.
 */
class ConfigParseNetemPacketLimitOptionValue : SimpleGrammarOptionValues(
    "config_parse_netem_packet_limit",
    SequenceCombinator(
        IntegerTerminal(0, 4294967295),
        EOF()
    )
)
