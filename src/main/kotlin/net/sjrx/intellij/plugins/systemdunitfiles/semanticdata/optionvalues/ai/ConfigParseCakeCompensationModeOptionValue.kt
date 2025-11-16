package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for CAKE.CompensationMode
 * C Function: config_parse_cake_compensation_mode(QDISC_KIND_CAKE)
 * Used by Options: CAKE.CompensationMode
 */
class ConfigParseCakeCompensationModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_cake_compensation_mode",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("none", "atm", "ptm"),
        EOF()
    )
)
