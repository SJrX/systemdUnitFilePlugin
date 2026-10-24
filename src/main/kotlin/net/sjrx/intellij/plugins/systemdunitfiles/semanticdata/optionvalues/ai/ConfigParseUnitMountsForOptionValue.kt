package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L3190-L3245   config_parse_unit_mounts_for
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34-L79   path_simplify_and_warn (PATH_CHECK_ABSOLUTE)
 */

/**
 * Validator for RequiresMountsFor= / WantsMountsFor=.
 *
 * C function: config_parse_unit_mounts_for in src/core/load-fragment.c. Tokenizes rvalue with
 * extract_first_word (whitespace-separated, EXTRACT_UNQUOTE), expands unit specifiers via
 * unit_path_printf, then requires each path to be absolute and normalized (no `..` component) via
 * path_simplify_and_warn with PATH_CHECK_ABSOLUTE. A bad word is skipped; the others still count.
 *
 * Each word must start with `/` or with a unit specifier (`%f`, `%h`, `%t/x`, ...): what a specifier
 * expands to is only known when the unit is loaded. Every `%X` in a word must be a specifier that
 * unit_path_printf knows ([BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS]); an unknown one makes systemd
 * drop the word. Quoted words are not modelled.
 */
class ConfigParseUnitMountsForOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_mounts_for",
    SequenceCombinator(
        RegexTerminal("\\S+", MOUNTS_FOR_WORD),
        ZeroOrMore(SequenceCombinator(
            WhitespaceTerminal(),
            RegexTerminal("\\S+", MOUNTS_FOR_WORD)
        )),
        EOF()
    )
) {
    private companion object {
        /** One absolute, normalized path (or one starting with a specifier), within a single word. */
        const val MOUNTS_FOR_WORD = "(?!(?:\\S*/)?\\.\\.(?:/|\\s|$))" +
            "(?:/|%[${BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS}])" +
            "(?:[^%\\s]|%%|%[${BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS}]|%(?![A-Za-z0-9%]))*"
    }
}
