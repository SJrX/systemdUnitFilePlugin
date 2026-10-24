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
        // parse_oom_score_adjust() is safe_atoi() plus a range check, so "+12", "0x10" and "\v5" are fine.
        // https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L763-L800
        // https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L762-L777
        SafeAtoTerminal(
            signed = true,
            base = 0,
            flags = emptySet(),
            minInclusive = -1000L,
            maxInclusive = 1000L,
            acceptsWhitespace = true,
            acceptsMinus = true,
        ),
        EOF()
    )
)
