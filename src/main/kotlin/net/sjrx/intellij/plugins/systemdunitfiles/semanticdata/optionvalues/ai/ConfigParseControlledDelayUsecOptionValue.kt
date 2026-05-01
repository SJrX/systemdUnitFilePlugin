package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for ControlledDelay.TargetSec, ControlledDelay.IntervalSec, ControlledDelay.CEThresholdSec
 * C Function: config_parse_codel_usec(QDISC_KIND_CODEL)
 *
 * Parses a time value via parse_sec(), accepting an integer optionally followed
 * by a time unit suffix (ms, us, µs, s, m, h, d, w, y).
 */
class ConfigParseControlledDelayUsecOptionValue : SimpleGrammarOptionValues(
    "config_parse_codel_usec",
    SequenceCombinator(
        RegexTerminal(
            "[0-9]+(?:ms|us|µs|s|m|h|d|w|y)?",
            "[0-9]+(?:ms|us|µs|s|m|h|d|w|y)?"
        ),
        EOF()
    )
)
