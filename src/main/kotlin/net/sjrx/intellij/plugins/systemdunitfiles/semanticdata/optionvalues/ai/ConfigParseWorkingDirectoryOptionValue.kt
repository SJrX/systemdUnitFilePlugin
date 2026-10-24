package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for WorkingDirectory=.
 *
 * C function: config_parse_working_directory,
 * https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L2656-L2715. Accepts:
 *   - optional leading "-" (missing directory is non-fatal)
 *   - then either "~" alone (the user's home) or a path, which goes through unit_path_printf() and
 *     path_simplify_and_warn(PATH_CHECK_ABSOLUTE): it must be absolute and, after path_simplify() drops leading
 *     "..", "." and empty components, contain no ".." component
 *     (https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34-L80). Whitespace inside
 *     the path is fine.
 *
 * A path that starts with a known specifier (%h, %S, ...) is only known once it is expanded, which depends on the
 * unit and the machine, so it is accepted; an unknown specifier anywhere is refused.
 */
class ConfigParseWorkingDirectoryOptionValue : SimpleGrammarOptionValues(
    "config_parse_working_directory",
    SequenceCombinator(
        ZeroOrOne(LiteralChoiceTerminal("-")),
        AlternativeCombinator(
            LiteralChoiceTerminal("~"),
            // A path that starts with a known specifier (%h, %S, ...) is only known once it is expanded; an unknown
            // specifier (%Q) makes unit_path_printf() fail, and %% is a literal '%' (so "%%/a" is relative).
            // https://github.com/systemd/systemd/blob/0f0682b773/src/shared/specifier.c#L33-L105
            RegexTerminal("%.*", "%[${BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS}]${BatchUnitCoreGrammar.UNIT_TEXT_CHAR}*"),
            // An absolute path: with a known specifier further on, only the specifiers are checked; without one,
            // also no ".." component after the leading ones path_simplify() drops.
            RegexTerminal(
                "/.*",
                "/(?=(?:[^%]|%%|%(?![A-Za-z0-9%]))*%[${BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS}])${BatchUnitCoreGrammar.UNIT_TEXT_CHAR}*\$" +
                    "|/(?:\\.{0,2}(?:/|$))*(?!(?:.*/)?\\.\\.(?:/|$))(?:[^%]|%%|%(?![A-Za-z0-9%]))*\$"
            )
        ),
        EOF()
    )
)
