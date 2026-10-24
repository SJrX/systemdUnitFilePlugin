package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.UseDomains (and the per-protocol UseDomains= keys)
 * C Function: config_parse_use_domains(0), src/network/networkd-dns.c
 *
 * DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN over use_domains_table (no, route, yes).
 * string_table_lookup_from_string_with_boolean tries parse_boolean() (case-insensitive) first,
 * then a case-sensitive lookup in the table, so only the boolean half ignores case.
 */
class ConfigParseUseDomainsOptionValue : SimpleGrammarOptionValues(
    "config_parse_use_domains",
    SequenceCombinator(
        namesOrBoolean("no", "route", "yes"),
        EOF()
    )
)
