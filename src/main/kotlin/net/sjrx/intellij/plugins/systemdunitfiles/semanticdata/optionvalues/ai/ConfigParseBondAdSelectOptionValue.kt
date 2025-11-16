package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Bond.AdSelect
 * C Function: config_parse_bond_ad_select(0)
 * Used by Options: Bond.AdSelect
 * 
 * Specifies the 802.3ad aggregation selection logic to use.
 * Valid values: stable, bandwidth, count
 */
class ConfigParseBondAdSelectOptionValue : SimpleGrammarOptionValues(
    "config_parse_bond_ad_select",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("stable", "bandwidth", "count"),
        EOF()
    )
)
