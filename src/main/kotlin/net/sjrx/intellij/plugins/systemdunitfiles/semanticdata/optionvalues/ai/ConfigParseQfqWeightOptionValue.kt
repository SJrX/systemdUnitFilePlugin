package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for QuickFairQueueingClass.Weight
 * C Function: config_parse_qfq_weight(TCLASS_KIND_QFQ)
 * Used by Options: QuickFairQueueingClass.Weight
 */
class ConfigParseQfqWeightOptionValue : SimpleGrammarOptionValues(
    "config_parse_qfq_weight",
    SequenceCombinator(
        IntegerTerminal(1, 1024),  // Range 1-1023 (max is exclusive)
        EOF()
    )
)
