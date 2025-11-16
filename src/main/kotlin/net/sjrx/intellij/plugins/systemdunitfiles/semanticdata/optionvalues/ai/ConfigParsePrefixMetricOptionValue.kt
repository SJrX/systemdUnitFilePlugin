package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for IPv6Prefix.RouteMetric
 * C Function: config_parse_prefix_metric(0)
 * Used by Options: IPv6Prefix.RouteMetric
 */
class ConfigParsePrefixMetricOptionValue : SimpleGrammarOptionValues(
    "config_parse_prefix_metric",
    SequenceCombinator(
        IntegerTerminal(0, 4294967296),  // 0 to 4294967295 (max is exclusive)
        EOF()
    )
)
