package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for [Link] MACAddress= (.network, .link).
 * C Function: config_parse_hw_addr (ltype 0) -> parse_hw_addr_full(expected_len = 0): a single 4/6/16/20-byte
 * hardware address in colon/hyphen/dot notation, or an IPv4/IPv6 address literal.
 */
class ConfigParseHwAddrOptionValue : SimpleGrammarOptionValues(
    "config_parse_hw_addr",
    SequenceCombinator(HARDWARE_ADDRESS, EOF())
)
