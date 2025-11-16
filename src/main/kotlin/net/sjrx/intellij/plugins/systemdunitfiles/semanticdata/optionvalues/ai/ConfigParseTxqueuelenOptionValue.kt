package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Link.TransmitQueueLength
 * C Function: config_parse_txqueuelen(0)
 * Used by Options: Link.TransmitQueueLength
 * 
 * Validates unsigned integers in the range 0 to 4294967294 (UINT32_MAX - 1).
 * The value UINT32_MAX (4294967295) is explicitly rejected by the C implementation.
 */
class ConfigParseTxqueuelenOptionValue : SimpleGrammarOptionValues(
    "config_parse_txqueuelen",
    SequenceCombinator(
        IntegerTerminal(0, 4294967295),  // 0 to 4294967294 (max is exclusive)
        EOF()
    )
)
