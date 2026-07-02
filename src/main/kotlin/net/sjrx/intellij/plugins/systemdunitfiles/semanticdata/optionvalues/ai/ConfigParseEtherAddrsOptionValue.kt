package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for [MACVLAN] SourceMACAddress= and [MACVTAP] SourceMACAddress= (.netdev).
 * C Function: config_parse_ether_addrs -> a whitespace-separated list of 6-byte Ethernet MACs.
 */
class ConfigParseEtherAddrsOptionValue : SimpleGrammarOptionValues(
    "config_parse_ether_addrs",
    SequenceCombinator(MAC_ADDRESS, ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), MAC_ADDRESS)), EOF())
)
