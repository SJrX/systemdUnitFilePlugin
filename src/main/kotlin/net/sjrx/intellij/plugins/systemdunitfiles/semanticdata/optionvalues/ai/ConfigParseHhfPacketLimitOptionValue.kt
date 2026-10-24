package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for HeavyHitterFilter.PacketLimit
 * C Function: config_parse_hhf_packet_limit(QDISC_KIND_HHF)
 * Used by Options: HeavyHitterFilter.PacketLimit
 * 
 * Validates that the value is a safe_atou32() unsigned integer in the range 0 to 4294967295.
 * This specifies the hard limit on the queue size in number of packets.
 * https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/hhf.c#L40-L90
 */
class ConfigParseHhfPacketLimitOptionValue : SimpleGrammarOptionValues(
    "config_parse_hhf_packet_limit",
    SequenceCombinator(
        SafeAtoTerminal(
            signed = false,
            base = 0,
            flags = emptySet(),
            minInclusive = 0L,
            maxInclusive = 4_294_967_295L,
            acceptsWhitespace = true,
            acceptsMinus = true,
        ),
        EOF()
    )
)
