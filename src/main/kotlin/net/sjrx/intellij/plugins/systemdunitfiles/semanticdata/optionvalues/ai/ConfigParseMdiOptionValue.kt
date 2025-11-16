package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Link.MDI
 * C Function: config_parse_mdi(0)
 * Used by Options: Link.MDI
 */
class ConfigParseMdiOptionValue : SimpleGrammarOptionValues(
    "config_parse_mdi",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("mdi", "straight", "mdi-x", "mdix", "crossover", "auto"),
        EOF()
    )
)
