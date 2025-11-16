package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Bond.PrimaryReselectPolicy
 * C Function: config_parse_bond_primary_reselect(0)
 * Used by Options: Bond.PrimaryReselectPolicy
 * 
 * Validates the reselection policy for the primary slave in bonding.
 * Valid values: always, better, failure
 */
class ConfigParseBondPrimaryReselectOptionValue : SimpleGrammarOptionValues(
    "config_parse_bond_primary_reselect",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("always", "better", "failure"),
        EOF()
    )
)
