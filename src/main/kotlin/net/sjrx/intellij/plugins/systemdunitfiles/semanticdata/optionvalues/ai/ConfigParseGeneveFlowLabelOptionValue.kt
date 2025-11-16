package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for GENEVE.FlowLabel
 * C Function: config_parse_geneve_flow_label(0)
 * Used by Options: GENEVE.FlowLabel
 * 
 * Validates a Geneve flow label, which must be an integer in the range [0-1048575].
 * The maximum value 1048575 is 0xFFFFF (20 bits), which is GENEVE_FLOW_LABEL_MAX_MASK.
 */
class ConfigParseGeneveFlowLabelOptionValue : SimpleGrammarOptionValues(
    "config_parse_geneve_flow_label",
    SequenceCombinator(
        IntegerTerminal(0, 1048576),  // [0, 1048575] - 20-bit flow label
        EOF()
    )
)
