package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for VXLAN.FlowLabel
 * C Function: config_parse_flow_label(0)
 * Used by Options: VXLAN.FlowLabel
 */
class ConfigParseFlowLabelOptionValue : SimpleGrammarOptionValues(
    "config_parse_flow_label",
    SequenceCombinator(
        IntegerTerminal(0, 1048576),  // Range 0-1048575 (max is exclusive)
        EOF()
    )
)
