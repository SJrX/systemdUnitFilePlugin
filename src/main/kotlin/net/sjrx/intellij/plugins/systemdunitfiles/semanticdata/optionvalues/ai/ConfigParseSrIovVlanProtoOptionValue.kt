package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for SR-IOV.VLANProtocol
 * C Function: config_parse_sr_iov_vlan_proto(0)
 * Used by Options: SR-IOV.VLANProtocol
 */
class ConfigParseSrIovVlanProtoOptionValue : SimpleGrammarOptionValues(
    "config_parse_sr_iov_vlan_proto",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("802.1Q", "802.1ad"),
        EOF()
    )
)
