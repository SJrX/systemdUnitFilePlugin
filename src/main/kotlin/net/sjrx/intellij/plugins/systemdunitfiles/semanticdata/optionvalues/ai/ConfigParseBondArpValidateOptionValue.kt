package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Bond.ARPValidate
 * C Function: config_parse_bond_arp_validate(0)
 * Used by Options: Bond.ARPValidate
 */
class ConfigParseBondArpValidateOptionValue : SimpleGrammarOptionValues(
    "config_parse_bond_arp_validate",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("none", "active", "backup", "all"),
        EOF()
    )
)
