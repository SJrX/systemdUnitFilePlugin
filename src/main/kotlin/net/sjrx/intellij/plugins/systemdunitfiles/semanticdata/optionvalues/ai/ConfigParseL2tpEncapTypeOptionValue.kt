package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for L2TP.EncapsulationType
 * C Function: config_parse_l2tp_encap_type(0)
 * Used by Options: L2TP.EncapsulationType
 */
class ConfigParseL2tpEncapTypeOptionValue : SimpleGrammarOptionValues(
    "config_parse_l2tp_encap_type",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("udp", "ip"),
        EOF()
    )
)
