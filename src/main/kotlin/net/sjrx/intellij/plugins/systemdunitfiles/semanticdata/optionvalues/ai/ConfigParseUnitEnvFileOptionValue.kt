package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for EnvironmentFile=.
 *
 * C function: config_parse_unit_env_file,
 * https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L2717-L2761. The whole value is one
 * path (whitespace included) after unit_path_printf() specifier expansion; an optional leading "-" marks the file
 * as missing-OK. The rest goes through path_simplify_and_warn(PATH_CHECK_ABSOLUTE): it must be absolute and,
 * after path_simplify() drops leading "..", "." and empty components, contain no ".." component
 * (https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34-L80).
 *
 * A path that starts with a specifier (%h, %E, ...) is only known once it is expanded, which depends on the unit
 * and the machine, so it is accepted.
 */
class ConfigParseUnitEnvFileOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_env_file",
    SequenceCombinator(
        ZeroOrOne(LiteralChoiceTerminal("-")),
        AlternativeCombinator(
            RegexTerminal("%.*", "%.*"),
            RegexTerminal("/.*", "/(?:\\.{0,2}(?:/|$))*(?!(?:.*/)?\\.\\.(?:/|$)).*")
        ),
        EOF()
    )
)
