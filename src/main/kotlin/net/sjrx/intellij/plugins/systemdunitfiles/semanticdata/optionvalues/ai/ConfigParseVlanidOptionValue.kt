package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for VLAN.Id
 * C Function: config_parse_vlanid(0)
 * Used by Options: VLAN.Id
 */
class ConfigParseVlanidOptionValue : SimpleGrammarOptionValues(
    "config_parse_vlanid",
    SequenceCombinator(
        IntegerTerminal(0, 4095),  // Range 0-4094 (max is exclusive)
        EOF()
    )
)
