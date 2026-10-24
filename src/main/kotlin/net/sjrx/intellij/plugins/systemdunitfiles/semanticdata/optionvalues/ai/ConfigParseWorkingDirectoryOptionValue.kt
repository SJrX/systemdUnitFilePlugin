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
 * A path that starts with a specifier (%h, %S, ...) is only known once it is expanded, which depends on the unit
 * and the machine, so it is accepted.
 */
class ConfigParseWorkingDirectoryOptionValue : SimpleGrammarOptionValues(
    "config_parse_working_directory",
    SequenceCombinator(
        ZeroOrOne(LiteralChoiceTerminal("-")),
        AlternativeCombinator(
            LiteralChoiceTerminal("~"),
            RegexTerminal("%.*", "%.*"),
            RegexTerminal("/.*", "/(?:\\.{0,2}(?:/|$))*(?!(?:.*/)?\\.\\.(?:/|$)).*")
        ),
        EOF()
    )
)
