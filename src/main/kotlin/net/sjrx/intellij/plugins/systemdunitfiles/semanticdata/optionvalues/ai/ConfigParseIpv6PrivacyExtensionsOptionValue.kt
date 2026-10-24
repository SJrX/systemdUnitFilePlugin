package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.IPv6PrivacyExtensions
 * C Function: config_parse_ipv6_privacy_extensions(0), src/network/networkd-sysctl.c
 *
 * DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN over ipv6_privacy_extensions_table
 * (no, prefer-public, yes, kernel).
 * string_table_lookup_from_string_with_boolean tries parse_boolean() (case-insensitive) first,
 * then a case-sensitive lookup in the table, so only the boolean half ignores case.
 */
class ConfigParseIpv6PrivacyExtensionsOptionValue : SimpleGrammarOptionValues(
    "config_parse_ipv6_privacy_extensions",
    SequenceCombinator(
        namesOrBoolean("no", "prefer-public", "yes", "kernel"),
        EOF()
    )
)
