package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Bridge.MulticastRouter
 * C Function: config_parse_multicast_router(0), src/network/netdev/bridge.c
 *
 * DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN(multicast_router, MulticastRouter, _MULTICAST_ROUTER_INVALID)
 * over multicast_router_table (no, query, permanent, temporary). A *true* boolean maps to
 * _MULTICAST_ROUTER_INVALID and is rejected, so only the false half of the booleans is accepted.
 * string_table_lookup_from_string_with_boolean tries parse_boolean() (case-insensitive) first,
 * then a case-sensitive lookup in the table, so only the boolean half ignores case.
 */
class ConfigParseMulticastRouterOptionValue : SimpleGrammarOptionValues(
    "config_parse_multicast_router",
    SequenceCombinator(
        PreferValidAlternativeCombinator(
            FlexibleLiteralChoiceTerminal("query", "permanent", "temporary"),
            BOOLEAN_FALSE,
        ),
        EOF()
    )
)
