package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L460-L516   config_parse_colon_separated_paths
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34-L79   path_simplify_and_warn (PATH_CHECK_ABSOLUTE)
 */

/**
 * Validator for ExecSearchPath= (and similar colon-separated path lists).
 *
 * C function: config_parse_colon_separated_paths in src/core/load-fragment.c. Tokenizes with
 * extract_first_word using ":" as the separator (no coalescing, so an empty entry is an error), expands
 * specifiers via unit_path_printf, then requires each path to be absolute and normalized (no `..`
 * component, except right after the root: `/../a` is simplified to `/a`) via path_simplify_and_warn with
 * PATH_CHECK_ABSOLUTE. Any failure drops the rest of the value.
 *
 * Spaces are ordinary path characters here (`/a b:/c` is fine). An entry may start with a unit specifier
 * (`%V`, `%h`), since what it expands to is only known when the unit is loaded; every `%X` must be a
 * specifier unit_path_printf knows ([BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS]).
 */
class ConfigParseColonSeparatedPathsOptionValue : SimpleGrammarOptionValues(
    "config_parse_colon_separated_paths",
    SequenceCombinator(
        RegexTerminal("[^:]+", SEARCH_PATH_ENTRY),
        ZeroOrMore(SequenceCombinator(
            LiteralChoiceTerminal(":"),
            RegexTerminal("[^:]+", SEARCH_PATH_ENTRY)
        )),
        EOF()
    )
) {
    private companion object {
        /** One absolute, normalized path (or one starting with a specifier), up to the next `:`. */
        const val SEARCH_PATH_ENTRY = "(?!(?>(?:/+(?:\\.{1,2}(?:/+|(?![^:])))*)?)(?:[^:]*/)?\\.\\.(?:/|:|$))" +
            "(?:/|%[${BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS}])" +
            "(?:[^%:]|%%|%[${BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS}]|%(?![A-Za-z0-9%]))*"
    }
}
