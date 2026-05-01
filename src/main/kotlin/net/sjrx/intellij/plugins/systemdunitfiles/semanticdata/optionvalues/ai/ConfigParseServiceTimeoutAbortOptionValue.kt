package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for Service.TimeoutAbortSec.
 * C Function: config_parse_service_timeout_abort(0)
 *
 * Delegates to config_parse_timeout_abort, which calls parse_sec — accepts
 * "infinity", a fractional or integer number with any of systemd's time-unit
 * suffixes, and compound forms like "1h 30s".
 */
class ConfigParseServiceTimeoutAbortOptionValue : SimpleGrammarOptionValues(
    "config_parse_service_timeout_abort",
    SequenceCombinator(TIME_VALUE, EOF())
)
