package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for RestrictNetworkInterfaces=.
 *
 * C function: config_parse_restrict_network_interfaces in src/core/load-fragment.c. Optional
 * leading "~" (invert / denylist mode), followed by a whitespace-separated list of interface
 * names that must pass ifname_valid_full(IFNAME_VALID_ALTERNATIVE):
 *   - 1..127 characters (ALTIFNAMSIZ - 1)
 *   - no whitespace, no '/', no ':', no '%' (rejected by ifname_valid_char)
 *   - cannot be "." or ".."
 *   - cannot be "all" or "default"
 *   - cannot be a purely-numeric string (interpreted as ifindex)
 *
 * The regex mirrors the existing config_parse_ifname validator but extends the length cap
 * from 15 (IFNAMSIZ) to 127 (ALTIFNAMSIZ).
 */
class ConfigParseRestrictNetworkInterfacesOptionValue : SimpleGrammarOptionValues(
    "config_parse_restrict_network_interfaces",
    SequenceCombinator(
        ZeroOrOne(LiteralChoiceTerminal("~")),
        IFNAME,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), IFNAME)),
        EOF()
    )
) {
    companion object {
        private val IFNAME = RegexTerminal(
            "(?!(?:all|default|\\.{1,2}|0[xX][0-9a-fA-F]+|[0-9]+)\\Z)[^\\s:/%]{1,127}",
            "(?!(?:all|default|\\.{1,2}|0[xX][0-9a-fA-F]+|[0-9]+)\\Z)[^\\s:/%]{1,127}"
        )
    }
}
