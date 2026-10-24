package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for VXLAN.IPDoNotFragment
 * C Function: config_parse_df(0), src/network/netdev/vxlan.c
 *
 * DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN(df, ...) over df_table (no, yes, inherit).
 * string_table_lookup_from_string_with_boolean tries parse_boolean() (case-insensitive) first,
 * then a case-sensitive lookup in the table, so only the boolean half ignores case.
 */
class ConfigParseDfOptionValue : SimpleGrammarOptionValues(
    "config_parse_df",
    SequenceCombinator(
        namesOrBoolean("no", "yes", "inherit"),
        EOF()
    )
)
