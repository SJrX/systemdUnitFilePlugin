package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IntegerTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/**
 * Validator for FairQueueing keys parsed by config_parse_fq_u32 (QDISC_KIND_FQ).
 * C Function: config_parse_fq_u32 - parses a uint32_t via safe_atou32.
 * Used by Options: FairQueueing.PacketLimit, FairQueueing.FlowLimit,
 *                  FairQueueing.Buckets, FairQueueing.OrphanMask.
 */
class ConfigParseFairQueueingU32OptionValue : SimpleGrammarOptionValues(
    "config_parse_fq_u32",
    SequenceCombinator(IntegerTerminal(0L, 4_294_967_296L), EOF())
)
