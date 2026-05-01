package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for TokenBucketFilter.LatencySec
 * C Function: config_parse_tbf_latency (QDISC_KIND_TBF)
 *
 * Calls parse_sec, which accepts "infinity", a fractional or integer number with any
 * of systemd's time-unit suffixes, and compound forms like "1h 30s".
 */
class ConfigParseTokenBucketFilterLatencyOptionValue : SimpleGrammarOptionValues(
    "config_parse_tbf_latency",
    SequenceCombinator(TIME_VALUE, EOF())
)
