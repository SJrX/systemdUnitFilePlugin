package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for [SR-IOV] MACAddress= (.network, .link).
 * C Function: config_parse_sr_iov_mac -> parse_ether_addr (a single 6-byte Ethernet MAC).
 */
class ConfigParseSrIovMacOptionValue : SimpleGrammarOptionValues(
    "config_parse_sr_iov_mac",
    SequenceCombinator(MAC_ADDRESS, EOF())
)
