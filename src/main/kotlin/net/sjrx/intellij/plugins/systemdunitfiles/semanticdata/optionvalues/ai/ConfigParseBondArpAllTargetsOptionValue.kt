package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Bond.ARPAllTargets
 * C Function: config_parse_bond_arp_all_targets(0)
 * Used by Options: Bond.ARPAllTargets
 */
class ConfigParseBondArpAllTargetsOptionValue : SimpleGrammarOptionValues(
    "config_parse_bond_arp_all_targets",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("any", "all"),
        EOF()
    )
)
