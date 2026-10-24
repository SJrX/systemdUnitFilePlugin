package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.KeepConfiguration
 * C Function: config_parse_keep_configuration(0), src/network/networkd-network.c
 *
 * First the backward-compatibility names `dhcp` and `dhcp-on-stop` (streq, case-sensitive), then
 * keep_configuration_from_string: DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN over
 * keep_configuration_table (no, dynamic-on-stop, dynamic, static, yes).
 * string_table_lookup_from_string_with_boolean tries parse_boolean() (case-insensitive) first,
 * then a case-sensitive lookup in the table, so only the boolean half ignores case.
 */
class ConfigParseKeepConfigurationOptionValue : SimpleGrammarOptionValues(
    "config_parse_keep_configuration",
    SequenceCombinator(
        namesOrBoolean("no", "dynamic-on-stop", "dynamic", "static", "yes", "dhcp", "dhcp-on-stop"),
        EOF()
    )
)
