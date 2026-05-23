package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for EnvironmentFile=.
 *
 * C function: config_parse_unit_env_file in src/core/load-fragment.c. Each line is a single
 * environment file path (after unit_path_printf specifier expansion); an optional leading "-"
 * marks the file as missing-OK. The resolved path must be absolute (path_simplify_and_warn
 * with PATH_CHECK_ABSOLUTE).
 */
class ConfigParseUnitEnvFileOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_env_file",
    SequenceCombinator(
        ZeroOrOne(LiteralChoiceTerminal("-")),
        RegexTerminal("/\\S*", "/\\S*"),
        EOF()
    )
)
