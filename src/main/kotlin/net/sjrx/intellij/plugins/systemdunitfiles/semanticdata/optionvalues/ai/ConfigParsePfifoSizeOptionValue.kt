package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for PFIFOHeadDrop.PacketLimit
 * C Function: config_parse_pfifo_size(QDISC_KIND_PFIFO_HEAD_DROP)
 * Used by Options: PFIFOHeadDrop.PacketLimit
 * 
 * Parses a packet limit value as an unsigned 32-bit integer.
 * Valid range: 0 to 4294967295 (2^32 - 1)
 */
class ConfigParsePfifoSizeOptionValue : SimpleGrammarOptionValues(
    "config_parse_pfifo_size",
    SequenceCombinator(
        IntegerTerminal(0, 4294967296),  // 0 to 2^32-1 (max exclusive)
        EOF()
    )
)
