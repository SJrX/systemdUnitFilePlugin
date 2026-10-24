package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Exec.Timezone (.nspawn)
 * C Function: config_parse_timezone_mode(0), src/nspawn/nspawn-settings.c
 *
 * DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN over timezone_mode_table
 * (off, copy, bind, symlink, delete, auto).
 * string_table_lookup_from_string_with_boolean tries parse_boolean() (case-insensitive) first,
 * then a case-sensitive lookup in the table, so only the boolean half ignores case.
 */
class ConfigParseTimezoneModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_timezone_mode",
    SequenceCombinator(
        PreferValidAlternativeCombinator(
            FlexibleLiteralChoiceTerminal("copy", "bind", "symlink", "delete", "auto"),
            BOOLEAN,
        ),
        EOF()
    )
)
