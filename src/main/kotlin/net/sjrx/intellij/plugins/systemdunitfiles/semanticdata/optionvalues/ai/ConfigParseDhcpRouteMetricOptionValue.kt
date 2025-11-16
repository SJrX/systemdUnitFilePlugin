package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for DHCPv4.RouteMetric
 * C Function: config_parse_dhcp_route_metric(AF_INET)
 * Used by Options: DHCPv4.RouteMetric
 */
class ConfigParseDhcpRouteMetricOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_route_metric",
    SequenceCombinator(
        IntegerTerminal(0, 4294967296L),  // 0 to 4294967295 (uint32_t max), maxExclusive so 4294967296
        EOF()
    )
)
