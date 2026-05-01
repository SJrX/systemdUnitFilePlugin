package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Unit.JobRunningTimeoutSec
 * C Function: config_parse_job_running_timeout_sec
 *
 * Parses a time value via parse_sec_fix_0(), accepting either the literal
 * "infinity" or an integer optionally followed by a time unit suffix
 * (ms, us, µs, s, m, h, d, w, y, min, sec, hour, day, week, year).
 */
class ConfigParseJobRunningTimeoutSecOptionValue : SimpleGrammarOptionValues(
    "config_parse_job_running_timeout_sec",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("infinity"),
            RegexTerminal(
                "[0-9]+(?:year|week|hour|day|min|sec|ms|us|µs|s|m|h|d|w|y)?",
                "[0-9]+(?:year|week|hour|day|min|sec|ms|us|µs|s|m|h|d|w|y)?"
            )
        ),
        EOF()
    )
)
