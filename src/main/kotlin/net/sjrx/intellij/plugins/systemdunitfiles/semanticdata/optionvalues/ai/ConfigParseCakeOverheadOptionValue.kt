package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for CAKE.OverheadBytes
 * C Function: config_parse_cake_overhead(QDISC_KIND_CAKE)
 * Used by Options: CAKE.OverheadBytes
 *
 * Validates integer values in the range [-64, 256].
 * The overhead bytes can be negative to account for hardware offloading.
 */
class ConfigParseCakeOverheadOptionValue : SimpleGrammarOptionValues(
    "config_parse_cake_overhead",
    SequenceCombinator(
        IntegerTerminal(-64, 257),  // Max is exclusive, so 257 for inclusive 256
        EOF()
    )
)
