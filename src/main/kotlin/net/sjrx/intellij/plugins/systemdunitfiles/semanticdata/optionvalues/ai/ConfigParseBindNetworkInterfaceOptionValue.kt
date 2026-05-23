package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for BindNetworkInterface=.
 *
 * C function: config_parse_bind_network_interface in src/core/load-fragment.c. After
 * unit_full_printf specifier expansion, the result must pass ifname_valid_full(
 * IFNAME_VALID_ALTERNATIVE) — same rules as RestrictNetworkInterfaces= entries but the value
 * is a single interface name (no whitespace-separated list, no leading "~").
 */
class ConfigParseBindNetworkInterfaceOptionValue : SimpleGrammarOptionValues(
    "config_parse_bind_network_interface",
    SequenceCombinator(
        RegexTerminal(
            "(?!(?:all|default|\\.{1,2}|0[xX][0-9a-fA-F]+|[0-9]+)\\Z)[^\\s:/%]{1,127}",
            "(?!(?:all|default|\\.{1,2}|0[xX][0-9a-fA-F]+|[0-9]+)\\Z)[^\\s:/%]{1,127}"
        ),
        EOF()
    )
)
