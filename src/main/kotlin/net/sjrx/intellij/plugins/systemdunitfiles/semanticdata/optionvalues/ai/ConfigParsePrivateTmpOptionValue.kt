package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for PrivateTmp= (Service/Socket/Mount/Swap)
 * C Function: config_parse_private_tmp(0), src/core/load-fragment.c
 *
 * private_tmp_from_string: DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN over private_tmp_table
 * (no, connected, disconnected) in src/core/namespace.c. `connected` is what a true boolean maps to
 * and is accepted by the lookup even though the man page only documents booleans and `disconnected`.
 * string_table_lookup_from_string_with_boolean tries parse_boolean() (case-insensitive) first,
 * then a case-sensitive lookup in the table, so only the boolean half ignores case:
 * `PrivateTmp=True` is valid, `PrivateTmp=Disconnected` is not.
 */
class ConfigParsePrivateTmpOptionValue : SimpleGrammarOptionValues(
    "config_parse_private_tmp",
    SequenceCombinator(
        namesOrBoolean("no", "connected", "disconnected"),
        EOF()
    )
)
