package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for [NetDev] MACAddress= and [Peer] MACAddress= (.netdev).
 * C Function: config_parse_netdev_hw_addr (ltype ETH_ALEN) -> a single 6-byte Ethernet MAC.
 */
class ConfigParseNetdevHwAddrOptionValue : SimpleGrammarOptionValues(
    "config_parse_netdev_hw_addr",
    SequenceCombinator(MAC_ADDRESS, EOF())
)
