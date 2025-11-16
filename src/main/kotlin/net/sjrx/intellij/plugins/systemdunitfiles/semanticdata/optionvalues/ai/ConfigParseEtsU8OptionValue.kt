package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for EnhancedTransmissionSelection.Bands
 * C Function: config_parse_ets_u8(QDISC_KIND_ETS)
 * Used by Options: EnhancedTransmissionSelection.Bands
 * 
 * Validates that the value is an unsigned integer in the range 1-16.
 */
class ConfigParseEtsU8OptionValue : SimpleGrammarOptionValues(
    "config_parse_ets_u8",
    SequenceCombinator(
        IntegerTerminal(1, 17),  // Range 1-16 inclusive (max is exclusive)
        EOF()
    )
)
