package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for MACsecTransmitAssociation.UseForEncoding
 * C Function: config_parse_macsec_use_for_encoding(0)
 * Used by Options: MACsecTransmitAssociation.UseForEncoding
 */
class ConfigParseMacsecUseForEncodingOptionValue : SimpleGrammarOptionValues(
    "config_parse_macsec_use_for_encoding",
    SequenceCombinator(
        BOOLEAN,
        EOF()
    )
)
