package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for ControlledDelay.TargetSec, ControlledDelay.IntervalSec, ControlledDelay.CEThresholdSec
 * C Function: config_parse_codel_usec(QDISC_KIND_CODEL)
 *
 * Calls parse_sec, which accepts "infinity", a fractional or integer number with any
 * of systemd's time-unit suffixes, and compound forms like "1h 30s".
 */
class ConfigParseControlledDelayUsecOptionValue : SimpleGrammarOptionValues(
    "config_parse_codel_usec",
    SequenceCombinator(TIME_VALUE, EOF())
)
