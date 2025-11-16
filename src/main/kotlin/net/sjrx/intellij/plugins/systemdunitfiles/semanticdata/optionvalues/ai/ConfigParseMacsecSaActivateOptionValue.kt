package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for MACsecTransmitAssociation.Activate
 * C Function: config_parse_macsec_sa_activate(0)
 * Used by Options: MACsecTransmitAssociation.Activate
 */
class ConfigParseMacsecSaActivateOptionValue : SimpleGrammarOptionValues(
    "config_parse_macsec_sa_activate",
    SequenceCombinator(
        BOOLEAN,
        EOF()
    )
)
