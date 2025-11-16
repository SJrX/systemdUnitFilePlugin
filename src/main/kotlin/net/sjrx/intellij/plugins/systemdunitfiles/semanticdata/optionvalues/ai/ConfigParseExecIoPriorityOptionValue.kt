package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Swap.IOSchedulingPriority
 * C Function: config_parse_exec_io_priority(0)
 * Used by Options: Swap.IOSchedulingPriority
 */
class ConfigParseExecIoPriorityOptionValue : SimpleGrammarOptionValues(
    "config_parse_exec_io_priority",
    SequenceCombinator(
        IntegerTerminal(0, 8),
        EOF()
    )
)
