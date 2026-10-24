package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for GENEVE.IPDoNotFragment
 * C Function: config_parse_geneve_df(0), src/network/netdev/geneve.c
 *
 * DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN(geneve_df, ...) over geneve_df_table (no, yes, inherit).
 * string_table_lookup_from_string_with_boolean tries parse_boolean() (case-insensitive) first,
 * then a case-sensitive lookup in the table, so only the boolean half ignores case.
 */
class ConfigParseGeneveDfOptionValue : SimpleGrammarOptionValues(
    "config_parse_geneve_df",
    SequenceCombinator(
        PreferValidAlternativeCombinator(
            FlexibleLiteralChoiceTerminal("inherit"),
            BOOLEAN,
        ),
        EOF()
    )
)
