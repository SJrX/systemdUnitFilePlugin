package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Route-section options taking an unsigned 32-bit integer: Route.Metric (config_parse_route_priority,
 * safe_atou32) and Route.NextHop (config_parse_route_nexthop, a nexthop id).
 */
class ConfigParseRouteSectionUint32OptionValue : SimpleGrammarOptionValues(
    "config_parse_route_section",
    SequenceCombinator(IntegerTerminal(0, 4_294_967_296), EOF())
)
