package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Files.PrivateUsersOwnership (.nspawn)
 * C Function: config_parse_userns_ownership(0), src/nspawn/nspawn-settings.c
 *
 * DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN over user_namespace_ownership_table
 * (off, chown, map, foreign, auto).
 * string_table_lookup_from_string_with_boolean tries parse_boolean() (case-insensitive) first,
 * then a case-sensitive lookup in the table, so only the boolean half ignores case.
 */
class ConfigParseUsernsOwnershipOptionValue : SimpleGrammarOptionValues(
    "config_parse_userns_ownership",
    SequenceCombinator(
        PreferValidAlternativeCombinator(
            FlexibleLiteralChoiceTerminal("chown", "map", "foreign", "auto"),
            BOOLEAN,
        ),
        EOF()
    )
)
