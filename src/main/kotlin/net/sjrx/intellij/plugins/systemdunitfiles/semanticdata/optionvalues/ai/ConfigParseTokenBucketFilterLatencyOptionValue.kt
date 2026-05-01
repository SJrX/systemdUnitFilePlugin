package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for TokenBucketFilter.LatencySec
 * C Function: config_parse_tbf_latency (QDISC_KIND_TBF)
 *
 * Uses parse_sec(), which accepts a non-negative integer optionally followed by
 * a systemd time unit suffix (ms, us, µs, s, m, h, d, w, y).
 */
class ConfigParseTokenBucketFilterLatencyOptionValue : SimpleGrammarOptionValues(
    "config_parse_tbf_latency",
    SequenceCombinator(
        RegexTerminal(
            "[0-9]+(?:ms|us|µs|s|m|h|d|w|y)?",
            "[0-9]+(?:ms|us|µs|s|m|h|d|w|y)?"
        ),
        EOF()
    )
)
