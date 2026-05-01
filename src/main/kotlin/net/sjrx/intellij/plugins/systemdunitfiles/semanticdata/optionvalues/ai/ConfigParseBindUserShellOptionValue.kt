package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Files.BindUserShell (.nspawn).
 * C Function: config_parse_bind_user_shell(0)
 *
 * The C path calls parse_user_shell which accepts either:
 *   - an absolute, normalized path (path_is_absolute && path_is_normalized), or
 *   - a boolean (parse_boolean)
 */
class ConfigParseBindUserShellOptionValue : SimpleGrammarOptionValues(
    "config_parse_bind_user_shell",
    SequenceCombinator(
        AlternativeCombinator(
            BOOLEAN,
            RegexTerminal("/[^\\s]+", "/[^\\s]+")
        ),
        EOF()
    )
)
