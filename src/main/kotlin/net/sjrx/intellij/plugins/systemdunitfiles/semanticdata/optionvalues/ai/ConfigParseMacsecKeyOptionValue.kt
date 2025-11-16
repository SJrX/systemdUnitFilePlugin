package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for MACsecTransmitAssociation.Key
 * C Function: config_parse_macsec_key(0)
 * Used by Options: MACsecTransmitAssociation.Key
 * 
 * Validates a 128-bit MACsec encryption key encoded as a hexadecimal string.
 * The key must be exactly 32 hexadecimal characters (16 bytes = 128 bits).
 */
class ConfigParseMacsecKeyOptionValue : SimpleGrammarOptionValues(
    "config_parse_macsec_key",
    SequenceCombinator(
        RegexTerminal("[0-9a-fA-F]{32}", "[0-9a-fA-F]{32}"),
        ZeroOrMore(WhitespaceTerminal()),
        EOF()
    )
)
