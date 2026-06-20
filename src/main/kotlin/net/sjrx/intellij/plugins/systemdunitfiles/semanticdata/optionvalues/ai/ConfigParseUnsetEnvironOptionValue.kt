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
    RegexTerminal("[^=\\s]+", "[A-Za-z_][A-Za-z0-9_]*"),
    ZeroOrOne(SequenceCombinator(LiteralChoiceTerminal("="), RegexTerminal("[^\\s]*", "[^\\s]*")))
)

class ConfigParseUnsetEnvironOptionValue : SimpleGrammarOptionValues(
    "config_parse_unset_environ",
    SequenceCombinator(
        UNSET_ENVIRON_ENTRY,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), UNSET_ENVIRON_ENTRY)),
        EOF()
    )
)
