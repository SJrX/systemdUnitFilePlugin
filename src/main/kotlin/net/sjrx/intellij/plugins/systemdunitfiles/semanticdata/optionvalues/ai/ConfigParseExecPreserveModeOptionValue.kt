package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for RuntimeDirectoryPreserve= (Service/Socket/Mount/Swap)
 * C Function: config_parse_exec_preserve_mode(0), src/core/load-fragment.c
 *
 * DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN over exec_preserve_mode_table
 * (no, yes, restart, on-success) in src/core/execute.c.
 * string_table_lookup_from_string_with_boolean tries parse_boolean() (case-insensitive) first,
 * then a case-sensitive lookup in the table, so only the boolean half ignores case.
 */
class ConfigParseExecPreserveModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_exec_preserve_mode",
    SequenceCombinator(
        PreferValidAlternativeCombinator(
            FlexibleLiteralChoiceTerminal("restart", "on-success"),
            BOOLEAN,
        ),
        EOF()
    )
)
