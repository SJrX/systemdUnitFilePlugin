package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.IPMasquerade
 * C Function: config_parse_ip_masquerade(0)
 * Used by Options: Network.IPMasquerade
 * 
 * Accepts: ipv4, ipv6, both, no, and deprecated boolean values
 * (yes/true/on/1 map to ipv4, no/false/off/0 map to no)
 */
class ConfigParseIpMasqueradeOptionValue : SimpleGrammarOptionValues(
    "config_parse_ip_masquerade",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal(
            // Primary values
            "ipv4", "ipv6", "both", "no",
            // Deprecated boolean true values (map to ipv4)
            "1", "yes", "y", "true", "t", "on",
            // Deprecated boolean false values (map to no)
            "0", "n", "false", "f", "off"
        ),
        EOF()
    )
)
