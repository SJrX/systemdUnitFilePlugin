package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for IPVLAN.Mode
 * C Function: config_parse_ipvlan_mode(0)
 * Used by Options: IPVLAN.Mode
 *
 * Validates IPVLAN mode values: L2, L3, or L3S
 */
class ConfigParseIpvlanModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_ipvlan_mode",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("L2", "L3", "L3S"),
        EOF()
    )
)
