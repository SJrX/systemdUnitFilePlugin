package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for [BridgeFDB] MACAddress= (.network).
 * C Function: config_parse_fdb_hwaddr -> parse_ether_addr (a single 6-byte Ethernet MAC).
 */
class ConfigParseFdbHwaddrOptionValue : SimpleGrammarOptionValues(
    "config_parse_fdb_hwaddr",
    SequenceCombinator(MAC_ADDRESS, EOF())
)
