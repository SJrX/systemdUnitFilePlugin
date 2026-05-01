package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for Service.TimeoutAbortSec.
 * C Function: config_parse_service_timeout_abort(0)
 *
 * Delegates to config_parse_timeout_abort, which calls parse_sec on the value. An
 * empty value clears the setting (handled elsewhere); otherwise the value is a
 * non-negative integer optionally suffixed with a time unit, or the literal
 * "infinity".
 */
class ConfigParseServiceTimeoutAbortOptionValue : SimpleGrammarOptionValues(
    "config_parse_service_timeout_abort",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("infinity"),
            RegexTerminal(
                "[0-9]+(?:year|week|hour|day|min|sec|ms|us|µs|s|m|h|d|w|y)?",
                "[0-9]+(?:year|week|hour|day|min|sec|ms|us|µs|s|m|h|d|w|y)?"
            )
        ),
        EOF()
    )
)
