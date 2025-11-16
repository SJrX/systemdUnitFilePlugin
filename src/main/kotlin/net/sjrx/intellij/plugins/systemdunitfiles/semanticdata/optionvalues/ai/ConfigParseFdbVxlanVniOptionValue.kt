package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for BridgeFDB.VNI
 * C Function: config_parse_fdb_vxlan_vni(0)
 * Used by Options: BridgeFDB.VNI
 * 
 * Validates VXLAN Network Identifier (VNI) values.
 * Valid range: 1-16777215 (VXLAN_VID_MAX)
 */
class ConfigParseFdbVxlanVniOptionValue : SimpleGrammarOptionValues(
    "config_parse_fdb_vxlan_vni",
    SequenceCombinator(
        IntegerTerminal(1, 16777216),  // Range 1-16777215 (maxExclusive is 16777216)
        EOF()
    )
)
