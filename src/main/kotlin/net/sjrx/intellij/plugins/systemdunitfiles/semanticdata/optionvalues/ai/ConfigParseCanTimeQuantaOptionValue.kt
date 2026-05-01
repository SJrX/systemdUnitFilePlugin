package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for CAN.TimeQuantaNSec, CAN.DataTimeQuantaNSec.
 * C Function: config_parse_can_time_quanta(0)
 *
 * Per parse_nsec, accepts "infinity", a fractional or integer number with any of
 * systemd's time-unit suffixes (default unit: nanoseconds), and compound forms
 * like "1ms 500us".
 */
class ConfigParseCanTimeQuantaOptionValue : SimpleGrammarOptionValues(
    "config_parse_can_time_quanta",
    SequenceCombinator(TIME_VALUE, EOF())
)
