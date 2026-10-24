package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for UnsetEnvironment=.
 *
 * C function: config_parse_unset_environ in src/core/load-fragment.c. The value is a
 * whitespace-separated list where each entry must be either a valid environment variable name
 * (env_name_is_valid) or a full assignment (env_assignment_is_valid). An environment variable
 * name matches [A-Za-z_][A-Za-z0-9_]*; an assignment is "NAME=value" with an arbitrary value.
 *
 * Composed from per-token combinators (name, optional '=' value) repeated over whitespace so that
 * an invalid entry localizes to that entry rather than invalidating the whole value.
 */
private val UNSET_ENVIRON_ENTRY = SequenceCombinator(
    // A name with a known specifier (e.g. X%b) is only known once unit_env_printf() expands it, which depends on the
    // machine, so it is accepted. An unknown specifier (e.g. %Q), in the name or the value, makes unit_env_printf()
    // fail, so the entry is ignored.
    // https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L2905-L2972
    // https://github.com/systemd/systemd/blob/0f0682b773/src/shared/specifier.c#L33-L105
    RegexTerminal(
        "[^=\\s]+",
        "(?=(?:[^=\\s%]|%%|%(?![A-Za-z0-9%]))*%[${BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS}])" +
            "(?:[^=\\s%]|%%|%[${BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS}]|%(?![A-Za-z0-9%]))+" +
            "|[A-Za-z_][A-Za-z0-9_]*"
    ),
    ZeroOrOne(
        SequenceCombinator(
            LiteralChoiceTerminal("="),
            RegexTerminal("[^\\s]*", "(?:[^\\s%]|%%|%[${BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS}]|%(?![A-Za-z0-9%]))*")
        )
    )
)

class ConfigParseUnsetEnvironOptionValue : SimpleGrammarOptionValues(
    "config_parse_unset_environ",
    SequenceCombinator(
        UNSET_ENVIRON_ENTRY,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), UNSET_ENVIRON_ENTRY)),
        EOF()
    )
)
