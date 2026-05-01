package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Service.TimeoutSec, Service.TimeoutStartSec.
 * C Function: config_parse_service_timeout(0)
 *
 * Per parse_sec_fix_0 -> parse_sec: accepts a time value (with optional unit
 * suffix, possibly compound like "1min 30s") OR the literal "infinity".
 * A bare integer with no suffix (including 0) is interpreted as seconds.
 */
class ConfigParseServiceTimeoutOptionValue : SimpleGrammarOptionValues(
    "config_parse_service_timeout",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("infinity"),
            RegexTerminal(
                "[0-9]+(?:year|week|hour|day|min|sec|ms|us|µs|s|m|h|d|w|y)?(?:\\s+[0-9]+(?:year|week|hour|day|min|sec|ms|us|µs|s|m|h|d|w|y)?)*",
                "[0-9]+(?:year|week|hour|day|min|sec|ms|us|µs|s|m|h|d|w|y)?(?:\\s+[0-9]+(?:year|week|hour|day|min|sec|ms|us|µs|s|m|h|d|w|y)?)*"
            )
        ),
        EOF()
    )
)
