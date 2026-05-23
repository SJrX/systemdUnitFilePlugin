package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for WorkingDirectory=.
 *
 * C function: config_parse_working_directory in src/core/load-fragment.c. Accepts:
 *   - optional leading "-" (missing path is non-fatal)
 *   - then either "~" alone (use the user's home) OR an absolute path
 *
 * Path values go through unit_path_printf so "%X" specifiers may appear inline. The grammar
 * allows any non-whitespace characters in the path; tighter shell-metacharacter checks happen
 * in path_simplify_and_warn at runtime.
 */
class ConfigParseWorkingDirectoryOptionValue : SimpleGrammarOptionValues(
    "config_parse_working_directory",
    SequenceCombinator(
        ZeroOrOne(LiteralChoiceTerminal("-")),
        AlternativeCombinator(
            LiteralChoiceTerminal("~"),
            RegexTerminal("/\\S*", "/\\S*")
        ),
        EOF()
    )
)
