package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for CAKE.RTTSec (.network).
 * C Function: config_parse_cake_rtt(QDISC_KIND_CAKE)
 *
 * Calls parse_sec; accepts a positive systemd time value (ms/us/s/m/h/d/...).
 * Empty resets and is always valid (handled outside the grammar).
 */
class ConfigParseCakeRttOptionValue : SimpleGrammarOptionValues(
    "config_parse_cake_rtt",
    SequenceCombinator(
        RegexTerminal(
            "[0-9]+(?:year|week|hour|day|min|sec|ms|us|µs|s|m|h|d|w|y)?",
            "[0-9]+(?:year|week|hour|day|min|sec|ms|us|µs|s|m|h|d|w|y)?"
        ),
        EOF()
    )
)
