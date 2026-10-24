package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for DHCPServer.PersistLeases
 * C Function: config_parse_dhcp_server_persist_leases(0), src/network/networkd-dhcp-server.c
 *
 * DEFINE_PRIVATE_STRING_TABLE_LOOKUP_FROM_STRING_WITH_BOOLEAN over
 * dhcp_server_persist_leases_table (no, yes, runtime).
 * string_table_lookup_from_string_with_boolean tries parse_boolean() (case-insensitive) first,
 * then a case-sensitive lookup in the table, so only the boolean half ignores case.
 */
class ConfigParseDhcpServerPersistLeasesOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_server_persist_leases",
    SequenceCombinator(
        namesOrBoolean("no", "yes", "runtime"),
        EOF()
    )
)
