package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Service.TimeoutSec, Service.TimeoutStartSec.
 * C Function: config_parse_service_timeout(0)
 *
 * Calls parse_sec_fix_0 -> parse_sec, which accepts "infinity", a fractional or
 * integer number with any of systemd's time-unit suffixes, and compound forms
 * like "1min 30s".
 */
class ConfigParseServiceTimeoutOptionValue : SimpleGrammarOptionValues(
    "config_parse_service_timeout",
    SequenceCombinator(TIME_VALUE, EOF())
)
