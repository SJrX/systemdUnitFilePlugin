package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for L2TPSession.Layer2SpecificHeader
 * C Function: config_parse_l2tp_session_l2spec(0)
 * Used by Options: L2TPSession.Layer2SpecificHeader
 */
class ConfigParseL2tpSessionL2specOptionValue : SimpleGrammarOptionValues(
    "config_parse_l2tp_session_l2spec",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("none", "default"),
        EOF()
    )
)
