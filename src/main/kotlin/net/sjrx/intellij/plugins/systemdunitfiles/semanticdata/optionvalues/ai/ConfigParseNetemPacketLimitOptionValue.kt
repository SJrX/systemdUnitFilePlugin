package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for NetworkEmulator.PacketLimit
 * C Function: config_parse_netem_packet_limit(QDISC_KIND_NETEM)
 * Used by Options: NetworkEmulator.PacketLimit
 * 
 * Validates a safe_atou() unsigned integer in the range 0…4294967295 representing
 * the maximum number of packets the qdisc may hold queued at a time.
 * https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/netem.c#L172-L221
 */
class ConfigParseNetemPacketLimitOptionValue : SimpleGrammarOptionValues(
    "config_parse_netem_packet_limit",
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
