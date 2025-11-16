package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for BatmanAdvanced.GatewayMode
 * C Function: config_parse_batadv_gateway_mode(0)
 * Used by Options: BatmanAdvanced.GatewayMode
 * 
 * Valid values: off, client, server
 */
class ConfigParseBatadvGatewayModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_batadv_gateway_mode",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("off", "client", "server"),
        EOF()
    )
)
