package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for CAKE.PriorityQueueingPreset (.network).
 * C Function: config_parse_cake_priority_queueing_preset(QDISC_KIND_CAKE)
 *
 * Accepts the 5 entries in cake_priority_queueing_preset_table:
 * besteffort, precedence, diffserv3, diffserv4, diffserv8.
 */
class ConfigParseCakePriorityQueueingPresetOptionValue : SimpleGrammarOptionValues(
    "config_parse_cake_priority_queueing_preset",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("besteffort", "precedence", "diffserv3", "diffserv4", "diffserv8"),
        EOF()
    )
)
