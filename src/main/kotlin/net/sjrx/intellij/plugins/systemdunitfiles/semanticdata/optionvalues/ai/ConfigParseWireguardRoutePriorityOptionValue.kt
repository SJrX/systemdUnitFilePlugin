package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for WireGuard.RouteMetric
 * C Function: config_parse_wireguard_route_priority(0)
 * Used by Options: WireGuard.RouteMetric
 */
class ConfigParseWireguardRoutePriorityOptionValue : SimpleGrammarOptionValues(
    "config_parse_wireguard_route_priority",
    SequenceCombinator(
        IntegerTerminal(0, 4294967296),  // 0 to 4294967295 (maxExclusive is 4294967296)
        EOF()
    )
)
