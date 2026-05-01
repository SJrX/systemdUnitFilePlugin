package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Unit.JobRunningTimeoutSec
 * C Function: config_parse_job_running_timeout_sec
 *
 * Parses a time value via parse_sec_fix_0 -> parse_sec, which accepts "infinity",
 * a fractional or integer number with any of systemd's time-unit suffixes, and
 * compound forms like "1h 30s".
 */
class ConfigParseJobRunningTimeoutSecOptionValue : SimpleGrammarOptionValues(
    "config_parse_job_running_timeout_sec",
    SequenceCombinator(TIME_VALUE, EOF())
)
