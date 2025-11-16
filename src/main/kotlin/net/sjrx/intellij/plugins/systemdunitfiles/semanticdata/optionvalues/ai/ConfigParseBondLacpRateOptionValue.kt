package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Bond.LACPTransmitRate
 * C Function: config_parse_bond_lacp_rate(0)
 * Used by Options: Bond.LACPTransmitRate
 */
class ConfigParseBondLacpRateOptionValue : SimpleGrammarOptionValues(
    "config_parse_bond_lacp_rate",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("slow", "fast"),
        EOF()
    )
)
