package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Bond.FailOverMACPolicy
 * C Function: config_parse_bond_fail_over_mac(0)
 * Used by Options: Bond.FailOverMACPolicy
 * 
 * Validates the fail-over MAC policy for active-backup bonding mode.
 * Valid values are: none, active, follow
 */
class ConfigParseBondFailOverMacOptionValue : SimpleGrammarOptionValues(
    "config_parse_bond_fail_over_mac",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("none", "active", "follow"),
        EOF()
    )
)
