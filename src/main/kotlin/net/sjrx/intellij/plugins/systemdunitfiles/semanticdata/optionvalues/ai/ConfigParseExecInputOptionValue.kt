package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for StandardInput=.
 *
 * C function: config_parse_exec_input in src/core/load-fragment.c. Accepts:
 *   - "fd:NAME" — fdname_is_valid name (alphanumerics-ish, specifier-aware)
 *   - "file:/PATH" — absolute path (specifier-aware)
 *   - one of the bare enum names from exec_input_table in src/core/execute.c:
 *     null, tty, tty-force, tty-fail, socket, data
 *     (fd / file appear as prefixes above; their bare form isn't accepted)
 *
 * The structured branches (fd:/file:) come before the enum so that a literal "fd"/"file"
 * (which aren't in the enum table anyway) wouldn't accidentally match. Order also matters
 * because LiteralChoiceTerminal does first-match-by-length.
 */
class ConfigParseExecInputOptionValue : SimpleGrammarOptionValues(
    "config_parse_exec_input",
    SequenceCombinator(
        AlternativeCombinator(
            // fd:NAME (NAME is allowed to be empty per the C code's isempty check)
            SequenceCombinator(LiteralChoiceTerminal("fd:"), RegexTerminal("\\S*", "\\S*")),
            // file:/PATH
            SequenceCombinator(LiteralChoiceTerminal("file:"), RegexTerminal("/\\S*", "/\\S*")),
            LiteralChoiceTerminal("null", "tty", "tty-force", "tty-fail", "socket", "data")
        ),
        EOF()
    )
)
