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
        // Only a true boolean draws a warning: systemd maps it to ipv4 (not both) and says it is deprecated. A false
        // boolean silently means "no", which is also a table value, so it is not flagged.
        // https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-util.c#L176-L185
        val DEPRECATED_BOOLEAN = FlexibleLiteralChoiceTerminal(*BOOLEAN_SPELLINGS, ignoreCase = true).deprecating(
            listOf("1", "yes", "y", "true", "t", "on").associateWith {
                "IPMasquerade= with a true boolean is deprecated; systemd treats it as ipv4, not both. Use ipv4 or both."
            }
        )
    }
}
