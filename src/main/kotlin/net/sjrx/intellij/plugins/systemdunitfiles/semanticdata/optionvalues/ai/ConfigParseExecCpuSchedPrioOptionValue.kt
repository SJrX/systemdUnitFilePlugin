package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Swap.CPUSchedulingPriority
 * C Function: config_parse_exec_cpu_sched_prio(0)
 * Used by Options: Swap.CPUSchedulingPriority
 */
class ConfigParseExecCpuSchedPrioOptionValue : SimpleGrammarOptionValues(
    "config_parse_exec_cpu_sched_prio",
    SequenceCombinator(
        IntegerTerminal(0, 100),  // 0-99 inclusive (maxExclusive=100)
        EOF()
    )
)
