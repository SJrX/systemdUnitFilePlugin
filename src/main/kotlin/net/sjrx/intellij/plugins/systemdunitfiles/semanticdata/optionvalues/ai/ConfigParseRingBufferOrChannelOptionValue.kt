package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Link.RxMiniBufferSize
 * C Function: config_parse_ring_buffer_or_channel(0)
 * Used by Options: Link.RxMiniBufferSize
 * 
 * Accepts either:
 * - The literal string "max"
 * - An unsigned integer in the range 1 to 4294967295
 */
class ConfigParseRingBufferOrChannelOptionValue : SimpleGrammarOptionValues(
    "config_parse_ring_buffer_or_channel",
    SequenceCombinator(
        AlternativeCombinator(
            LiteralChoiceTerminal("max"),
            IntegerTerminal(1, 4294967296) // max exclusive, so 4294967295 is the highest valid value
        ),
        EOF()
    )
)
