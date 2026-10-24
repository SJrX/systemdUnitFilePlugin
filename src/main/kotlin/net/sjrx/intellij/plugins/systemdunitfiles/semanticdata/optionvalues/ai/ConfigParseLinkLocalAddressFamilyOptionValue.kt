package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.LinkLocalAddressing
 * C Function: config_parse_link_local_address_family(0), src/network/networkd-util.c
 *
 * link_local_address_family_from_string: the compat names `fallback` and `fallback-ipv4` (streq),
 * then address_family_from_string, a DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN over
 * address_family_table (no, yes, ipv4, ipv6).
 * string_table_lookup_from_string_with_boolean tries parse_boolean() (case-insensitive) first,
 * then a case-sensitive lookup in the table, so only the boolean half ignores case.
 */
class ConfigParseLinkLocalAddressFamilyOptionValue : SimpleGrammarOptionValues(
    "config_parse_link_local_address_family",
    SequenceCombinator(
        PreferValidAlternativeCombinator(
            FlexibleLiteralChoiceTerminal("ipv4", "ipv6", "fallback", "fallback-ipv4"),
            BOOLEAN,
        ),
        EOF()
    )
)
