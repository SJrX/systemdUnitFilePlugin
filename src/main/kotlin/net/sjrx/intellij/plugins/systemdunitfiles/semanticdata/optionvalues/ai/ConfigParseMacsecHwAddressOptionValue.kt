package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for [MACsecReceiveChannel] MACAddress= and [MACsecReceiveAssociation] MACAddress= (.netdev).
 * C Function: config_parse_macsec_hw_address -> parse_ether_addr (a single 6-byte Ethernet MAC).
 */
class ConfigParseMacsecHwAddressOptionValue : SimpleGrammarOptionValues(
    "config_parse_macsec_hw_address",
    SequenceCombinator(MAC_ADDRESS, EOF())
)
