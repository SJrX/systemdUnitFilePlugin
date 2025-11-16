package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for BatmanAdvanced.RoutingAlgorithm
 * C Function: config_parse_batadv_routing_algorithm(0)
 * Used by Options: BatmanAdvanced.RoutingAlgorithm
 */
class ConfigParseBatadvRoutingAlgorithmOptionValue : SimpleGrammarOptionValues(
    "config_parse_batadv_routing_algorithm",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("batman-v", "batman-iv"),
        EOF()
    )
)
