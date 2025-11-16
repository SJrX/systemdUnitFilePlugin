package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for ControlledDelay.PacketLimit
 * C Function: config_parse_codel_u32(QDISC_KIND_CODEL)
 * Used by Options: ControlledDelay.PacketLimit
 * 
 * Validates an unsigned 32-bit integer in the range 0 to 4294967294.
 * This specifies the hard limit on the queue size in number of packets.
 */
class ConfigParseCodelU32OptionValue : SimpleGrammarOptionValues(
    "config_parse_codel_u32",
    SequenceCombinator(
        IntegerTerminal(0, 4294967295),  // Range 0 to 4294967294 inclusive (max is exclusive)
        EOF()
    )
)
