package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Boolean route metrics: Route.QuickAck and Route.FastOpenNoCookie
 * (config_parse_route_metric_boolean).
 */
class ConfigParseRouteSectionBooleanOptionValue : SimpleGrammarOptionValues(
    "config_parse_route_section",
    SequenceCombinator(BOOLEAN, EOF())
)
