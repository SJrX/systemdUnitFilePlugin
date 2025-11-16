package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for MACVLAN.BroadcastQueueThreshold
 * C Function: config_parse_macvlan_broadcast_queue_threshold(0)
 * Used by Options: MACVLAN.BroadcastQueueThreshold
 */
class ConfigParseMacvlanBroadcastQueueThresholdOptionValue : SimpleGrammarOptionValues(
    "config_parse_macvlan_broadcast_queue_threshold",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("no"),
            IntegerTerminal(0, 2147483648)  // 0 to 2147483647 inclusive (max exclusive)
        ),
        EOF()
    )
)
