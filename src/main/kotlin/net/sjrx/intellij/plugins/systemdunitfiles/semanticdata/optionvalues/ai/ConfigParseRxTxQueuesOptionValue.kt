package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Link.ReceiveQueues
 * C Function: config_parse_rx_tx_queues(0)
 * Used by Options: Link.ReceiveQueues
 *
 * Accepts an integer in the range 1-4096.
 */
class ConfigParseRxTxQueuesOptionValue : SimpleGrammarOptionValues(
    "config_parse_rx_tx_queues",
    SequenceCombinator(
        IntegerTerminal(1, 4097),  // min inclusive, max exclusive
        EOF()
    )
)
