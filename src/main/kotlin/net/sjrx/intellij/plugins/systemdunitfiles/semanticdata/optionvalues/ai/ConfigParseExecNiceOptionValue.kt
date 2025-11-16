package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Swap.Nice
 * C Function: config_parse_exec_nice(0)
 * Used by Options: Swap.Nice
 * 
 * Validates nice level (scheduling priority) values between -20 (highest priority) 
 * and 19 (lowest priority).
 */
class ConfigParseExecNiceOptionValue : SimpleGrammarOptionValues(
    "config_parse_exec_nice",
    SequenceCombinator(
        IntegerTerminal(-20, 20),  // -20 to 19 inclusive (max is exclusive)
        EOF()
    )
)
