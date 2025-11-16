package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for MACVLAN.Mode
 * C Function: config_parse_macvlan_mode(0)
 * Used by Options: MACVLAN.Mode
 */
class ConfigParseMacvlanModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_macvlan_mode",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("private", "vepa", "bridge", "passthru", "source"),
        EOF()
    )
)
