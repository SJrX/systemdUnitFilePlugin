package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Address.RouteMetric
 * C Function: config_parse_address_section(ADDRESS_ROUTE_METRIC)
 * Used by Options: Address.RouteMetric
 * 
 * Validates unsigned 32-bit integers in the range 0 to 4294967295.
 * This represents the metric of the prefix route for the configured IP address.
 */
class ConfigParseAddressSectionOptionValue : SimpleGrammarOptionValues(
    "config_parse_address_section",
    SequenceCombinator(
        IntegerTerminal(0, 4294967296),  // 0 to 4294967295 inclusive (max is exclusive)
        EOF()
    )
)
