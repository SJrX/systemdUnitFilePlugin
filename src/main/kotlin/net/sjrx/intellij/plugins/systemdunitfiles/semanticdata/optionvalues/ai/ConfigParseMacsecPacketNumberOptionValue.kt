package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for MACsecTransmitAssociation.PacketNumber
 * C Function: config_parse_macsec_packet_number(0)
 * Used by Options: MACsecTransmitAssociation.PacketNumber
 */
class ConfigParseMacsecPacketNumberOptionValue : SimpleGrammarOptionValues(
    "config_parse_macsec_packet_number",
    SequenceCombinator(
        IntegerTerminal(1, 4294967296),  // 1 to 4,294,967,295 (2^32-1) inclusive
        EOF()
    )
)
