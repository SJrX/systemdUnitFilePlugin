package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Swap.OOMScoreAdjust
 * C Function: config_parse_exec_oom_score_adjust(0)
 * Used by Options: Swap.OOMScoreAdjust
 * 
 * Validates OOM score adjustment values, which must be integers between -1000 and 1000 (inclusive).
 * -1000 disables OOM killing for processes of this unit, while 1000 makes killing very likely.
 */
class ConfigParseExecOomScoreAdjustOptionValue : SimpleGrammarOptionValues(
    "config_parse_exec_oom_score_adjust",
    SequenceCombinator(
        IntegerTerminal(-1000, 1001),  // -1000 to 1000 inclusive (max is exclusive)
        EOF()
    )
)
