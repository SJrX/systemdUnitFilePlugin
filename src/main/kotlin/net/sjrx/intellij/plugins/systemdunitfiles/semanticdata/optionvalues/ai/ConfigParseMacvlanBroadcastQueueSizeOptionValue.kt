package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for MACVLAN.BroadcastMulticastQueueLength
 * C Function: config_parse_macvlan_broadcast_queue_size(0)
 * Used by Options: MACVLAN.BroadcastMulticastQueueLength
 */
class ConfigParseMacvlanBroadcastQueueSizeOptionValue : SimpleGrammarOptionValues(
    "config_parse_macvlan_broadcast_queue_size",
    SequenceCombinator(
        IntegerTerminal(0, 4294967295),
        EOF()
    )
)
