package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for MACsecReceiveChannel.Port
 * C Function: config_parse_macsec_port(0)
 * Used by Options: MACsecReceiveChannel.Port
 * 
 * Validates port numbers for MACsec receive channels. The port is used to make
 * secure channel identifier (SCI). Valid range is 1-65535.
 */
class ConfigParseMacsecPortOptionValue : SimpleGrammarOptionValues(
    "config_parse_macsec_port",
    SequenceCombinator(
        IntegerTerminal(1, 65536),  // 1 to 65535 inclusive (max is exclusive)
        EOF()
    )
)
