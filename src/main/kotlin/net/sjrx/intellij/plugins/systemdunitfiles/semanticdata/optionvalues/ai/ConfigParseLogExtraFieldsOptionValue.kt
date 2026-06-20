package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for LogExtraFields=.
 *
 * C function: config_parse_log_extra_fields in src/core/load-fragment.c. The value is a
 * whitespace-separated list of "FIELD=value" entries; each FIELD must be a valid journal field
 * name (journal_field_valid, src/libsystemd/sd-journal): 1-64 characters, only A-Z 0-9 '_', not
 * starting with a digit or '_'. The value after '=' is arbitrary (binary-safe). Each entry must
 * contain a '='.
 *
 * Composed from per-token combinators (field name, '=', value) repeated over whitespace so that an
 * invalid entry localizes to that entry rather than invalidating the whole value.
 */
private val LOG_EXTRA_FIELD_ENTRY = SequenceCombinator(
    RegexTerminal("[^=\\s]+", "[A-Z][A-Z0-9_]{0,63}"),
    LiteralChoiceTerminal("="),
    RegexTerminal("[^\\s]*", "[^\\s]*")
)

class ConfigParseLogExtraFieldsOptionValue : SimpleGrammarOptionValues(
    "config_parse_log_extra_fields",
    SequenceCombinator(
        LOG_EXTRA_FIELD_ENTRY,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), LOG_EXTRA_FIELD_ENTRY)),
        EOF()
    )
)
