package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for CAKE.RTTSec (.network).
 * C Function: config_parse_cake_rtt(QDISC_KIND_CAKE)
 *
 * Calls parse_sec, which accepts "infinity", a fractional or integer number
 * with any of systemd's time-unit suffixes, and compound forms like "1h 30s".
 */
class ConfigParseCakeRttOptionValue : SimpleGrammarOptionValues(
    "config_parse_cake_rtt",
    SequenceCombinator(TIME_VALUE, EOF())
)
