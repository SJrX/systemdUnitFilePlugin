package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for CAN.TimeQuantaNSec, CAN.DataTimeQuantaNSec.
 * C Function: config_parse_can_time_quanta(0)
 *
 * Per parse_nsec: accepts a time value (with optional unit suffix, possibly
 * compound like "1ms 500us") OR the literal "infinity". A bare integer with no
 * suffix is interpreted as nanoseconds.
 */
class ConfigParseCanTimeQuantaOptionValue : SimpleGrammarOptionValues(
    "config_parse_can_time_quanta",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("infinity"),
            RegexTerminal(
                "[0-9]+(?:ns|nsec|us|µs|μs|ms|msec|s|sec|seconds?|m|min|minutes?|h|hr|hours?|d|days?|w|weeks?|M|months?|y|years?)?(?:\\s+[0-9]+(?:ns|nsec|us|µs|μs|ms|msec|s|sec|seconds?|m|min|minutes?|h|hr|hours?|d|days?|w|weeks?|M|months?|y|years?)?)*",
                "[0-9]+(?:ns|nsec|us|µs|μs|ms|msec|s|sec|seconds?|m|min|minutes?|h|hr|hours?|d|days?|w|weeks?|M|months?|y|years?)?(?:\\s+[0-9]+(?:ns|nsec|us|µs|μs|ms|msec|s|sec|seconds?|m|min|minutes?|h|hr|hours?|d|days?|w|weeks?|M|months?|y|years?)?)*"
            )
        ),
        EOF()
    )
)
