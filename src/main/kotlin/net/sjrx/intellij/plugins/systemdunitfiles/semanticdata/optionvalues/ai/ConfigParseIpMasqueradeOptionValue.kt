package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.IPMasquerade
 * C Function: config_parse_ip_masquerade(0), src/network/networkd-util.c
 *
 * Calls parse_boolean() first (case-insensitive; yes/true/on/1 map to ipv4, no/false/off/0 to no),
 * then ip_masquerade_address_family_from_string, a plain case-sensitive lookup over
 * ip_masquerade_address_family_table (no, both, ipv4, ipv6).
 */
class ConfigParseIpMasqueradeOptionValue : SimpleGrammarOptionValues(
    "config_parse_ip_masquerade",
    SequenceCombinator(
        PreferValidAlternativeCombinator(
            FlexibleLiteralChoiceTerminal("ipv4", "ipv6", "both"),
            DEPRECATED_BOOLEAN
        ),
        EOF()
    )
) {
    private companion object {
        // systemd.network(5): the boolean forms "are now deprecated. Please use one of the values
        // above" (ipv4/ipv6/both/no). Still accepted; the truthy forms map to ipv4.
        val DEPRECATED_BOOLEAN = FlexibleLiteralChoiceTerminal(*BOOLEAN_SPELLINGS, ignoreCase = true).deprecating(
            buildMap {
                for (t in listOf("1", "yes", "y", "true", "t", "on")) {
                    put(t, "Boolean values for IPMasquerade= are deprecated; use \"ipv4\" (or \"ipv6\"/\"both\") instead.")
                }
                for (f in listOf("0", "n", "false", "f", "off")) {
                    put(f, "Boolean values for IPMasquerade= are deprecated; use \"no\" instead.")
                }
            }
        )
    }
}
