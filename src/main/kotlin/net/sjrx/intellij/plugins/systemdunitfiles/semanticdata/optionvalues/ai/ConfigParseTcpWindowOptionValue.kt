package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for DHCPv4.InitialAdvertisedReceiveWindow
 * C Function: config_parse_tcp_window(0)
 * Used by Options: DHCPv4.InitialAdvertisedReceiveWindow
 * 
 * Validates TCP window size values. The value must be an integer in the range [1, 1023].
 * Values of 0 or >= 1024 are rejected by the C implementation.
 */
class ConfigParseTcpWindowOptionValue : SimpleGrammarOptionValues(
    "config_parse_tcp_window",
    SequenceCombinator(
        IntegerTerminal(1, 1024),  // min=1 inclusive, max=1024 exclusive, so range is [1, 1023]
        EOF()
    )
)
