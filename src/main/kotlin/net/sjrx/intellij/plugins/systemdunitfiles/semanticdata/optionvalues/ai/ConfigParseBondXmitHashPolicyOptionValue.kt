package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Bond.TransmitHashPolicy
 * C Function: config_parse_bond_xmit_hash_policy(0)
 * Used by Options: Bond.TransmitHashPolicy
 */
class ConfigParseBondXmitHashPolicyOptionValue : SimpleGrammarOptionValues(
    "config_parse_bond_xmit_hash_policy",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("layer2", "layer3+4", "layer2+3", "encap2+3", "encap3+4"),
        EOF()
    )
)
