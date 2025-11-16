package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for IPVLAN.Flags
 * C Function: config_parse_ipvlan_flags(0)
 * Used by Options: IPVLAN.Flags
 * 
 * Validates IPVLAN flags. Valid values are:
 * - bridge
 * - private
 * - vepa
 */
class ConfigParseIpvlanFlagsOptionValue : SimpleGrammarOptionValues(
    "config_parse_ipvlan_flags",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("bridge", "private", "vepa"),
        EOF()
    )
)
