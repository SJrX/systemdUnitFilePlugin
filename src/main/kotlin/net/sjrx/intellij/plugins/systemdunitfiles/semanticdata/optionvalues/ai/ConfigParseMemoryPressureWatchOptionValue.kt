package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Swap.MemoryPressureWatch
 * C Function: config_parse_memory_pressure_watch(0)
 * Used by Options: Swap.MemoryPressureWatch
 * 
 * Accepts boolean values (yes/no/1/0/true/false/on/off/y/n/t/f) or special values (auto/skip).
 */
class ConfigParseMemoryPressureWatchOptionValue : SimpleGrammarOptionValues(
    "config_parse_memory_pressure_watch",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("yes", "no", "1", "0", "true", "false", "on", "off", "y", "n", "t", "f", "auto", "skip"),
        EOF()
    )
)
