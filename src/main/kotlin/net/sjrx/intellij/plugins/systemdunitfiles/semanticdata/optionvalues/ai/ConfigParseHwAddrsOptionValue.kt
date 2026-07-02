package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for [Match] MACAddress= and [Match] PermanentMACAddress= (.network, .link).
 * C Function: config_parse_hw_addrs (ltype 0) -> a whitespace-separated list of hardware addresses, each
 * a 4/6/16/20-byte address in colon/hyphen/dot notation, or an IPv4/IPv6 address literal.
 */
class ConfigParseHwAddrsOptionValue : SimpleGrammarOptionValues(
    "config_parse_hw_addrs",
    SequenceCombinator(HARDWARE_ADDRESS, ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), HARDWARE_ADDRESS)), EOF())
)
