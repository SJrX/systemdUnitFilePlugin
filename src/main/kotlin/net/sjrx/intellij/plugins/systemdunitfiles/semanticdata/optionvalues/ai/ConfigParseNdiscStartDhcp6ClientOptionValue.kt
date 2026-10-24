package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for IPv6AcceptRA.DHCPv6Client
 * C Function: config_parse_ndisc_start_dhcp6_client(0), src/network/networkd-ndisc.c
 *
 * DEFINE_PRIVATE_STRING_TABLE_LOOKUP_FROM_STRING_WITH_BOOLEAN over ndisc_start_dhcp6_client_table
 * (no, always, yes).
 * string_table_lookup_from_string_with_boolean tries parse_boolean() (case-insensitive) first,
 * then a case-sensitive lookup in the table, so only the boolean half ignores case.
 */
class ConfigParseNdiscStartDhcp6ClientOptionValue : SimpleGrammarOptionValues(
    "config_parse_ndisc_start_dhcp6_client",
    SequenceCombinator(
        namesOrBoolean("no", "always", "yes"),
        EOF()
    )
)
