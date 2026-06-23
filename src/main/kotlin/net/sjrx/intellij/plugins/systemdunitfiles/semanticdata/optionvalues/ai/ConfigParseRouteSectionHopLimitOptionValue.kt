package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Route.HopLimit: an unsigned integer in 1..255 (config_parse_route_metric_hop_limit rejects 0 and
 * values above 255).
 */
class ConfigParseRouteSectionHopLimitOptionValue : SimpleGrammarOptionValues(
    "config_parse_route_section",
    SequenceCombinator(IntegerTerminal(1, 256), EOF())
)
