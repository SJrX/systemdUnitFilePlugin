package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.IPv6LinkLocalAddressGenerationMode
 * C Function: config_parse_ipv6_link_local_address_gen_mode(0)
 * Used by Options: Network.IPv6LinkLocalAddressGenerationMode
 */
class ConfigParseIpv6LinkLocalAddressGenModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_ipv6_link_local_address_gen_mode",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("eui64", "none", "stable-privacy", "random"),
        EOF()
    )
)
