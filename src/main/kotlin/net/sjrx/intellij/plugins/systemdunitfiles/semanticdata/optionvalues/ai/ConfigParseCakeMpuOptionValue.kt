package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for CAKE.MPUBytes
 * C Function: config_parse_cake_mpu(QDISC_KIND_CAKE)
 * Used by Options: CAKE.MPUBytes
 * 
 * Validates integer values in the range 1-256 (inclusive).
 */
class ConfigParseCakeMpuOptionValue : SimpleGrammarOptionValues(
    "config_parse_cake_mpu",
    SequenceCombinator(
        IntegerTerminal(1, 257),  // Range 1-256 inclusive (maxExclusive is 257)
        EOF()
    )
)
