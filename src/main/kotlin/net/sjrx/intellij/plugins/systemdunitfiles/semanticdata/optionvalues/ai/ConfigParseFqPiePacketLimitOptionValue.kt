package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for FlowQueuePIE.PacketLimit
 * C Function: config_parse_fq_pie_packet_limit(QDISC_KIND_FQ_PIE)
 * Used by Options: FlowQueuePIE.PacketLimit
 */
class ConfigParseFqPiePacketLimitOptionValue : SimpleGrammarOptionValues(
    "config_parse_fq_pie_packet_limit",
    SequenceCombinator(
        IntegerTerminal(1, 4294967295),
        EOF()
    )
)
