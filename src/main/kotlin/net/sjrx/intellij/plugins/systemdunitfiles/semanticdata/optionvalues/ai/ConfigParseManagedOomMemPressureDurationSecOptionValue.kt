package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for ManagedOOMMemoryPressureDurationSec=.
 *
 * C function: config_parse_managed_oom_mem_pressure_duration_sec in src/core/load-fragment.c.
 * Empty resets to USEC_INFINITY. Otherwise calls parse_sec(rvalue) and additionally requires
 * the result to be at least 1s and strictly less than infinity.
 *
 * The grammar matches parse_sec syntax (which is the existing TIME_VALUE: "infinity" or a
 * compound time expression). The semantic "must be >= 1s and != infinity" can't be enforced
 * by a grammar without arithmetic — leaving that as a runtime check (the systemd parser will
 * still log a warning at unit load).
 */
class ConfigParseManagedOomMemPressureDurationSecOptionValue : SimpleGrammarOptionValues(
    "config_parse_managed_oom_mem_pressure_duration_sec",
    SequenceCombinator(
        OptionalWhitespacePrefix(TIME_VALUE),
        EOF()
    )
)
