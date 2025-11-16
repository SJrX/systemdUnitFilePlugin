package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Swap.PrivateTmp
 * C Function: config_parse_private_tmp(0)
 * Used by Options: Swap.PrivateTmp
 * 
 * Takes a boolean argument, or disconnected. Valid values are:
 * - Boolean true: 1, yes, y, true, t, on
 * - Boolean false: 0, no, n, false, f, off
 * - disconnected
 */
class ConfigParsePrivateTmpOptionValue : SimpleGrammarOptionValues(
    "config_parse_private_tmp",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("1", "yes", "y", "true", "t", "on", "0", "no", "n", "false", "f", "off", "disconnected"),
        EOF()
    )
)
