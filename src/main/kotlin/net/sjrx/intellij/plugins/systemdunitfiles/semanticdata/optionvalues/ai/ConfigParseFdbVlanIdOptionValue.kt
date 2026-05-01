package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IntegerTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/**
 * Validator for BridgeFDB.VLANId
 * C Function: config_parse_fdb_vlan_id(0)
 * Used by Options: BridgeFDB.VLANId
 *
 * Delegates to config_parse_vlanid which accepts a uint16 in the inclusive range
 * 0..VLANID_MAX (4094).
 */
class ConfigParseFdbVlanIdOptionValue : SimpleGrammarOptionValues(
    "config_parse_fdb_vlan_id",
    SequenceCombinator(
        IntegerTerminal(0, 4095),  // Range 0-4094 inclusive (max is exclusive)
        EOF()
    )
)
