package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Swap.Priority
 * C Function: config_parse_swap_priority(0)
 * Used by Options: Swap.Priority
 * 
 * Validates swap priority values which must be integers in the range [-1, 32767].
 * Values less than -1 or greater than 32767 are rejected.
 */
class ConfigParseSwapPriorityOptionValue : SimpleGrammarOptionValues(
    "config_parse_swap_priority",
    SequenceCombinator(
        IntegerTerminal(-1, 32768),  // Max is exclusive, so 32768 allows up to 32767
        EOF()
    )
)
