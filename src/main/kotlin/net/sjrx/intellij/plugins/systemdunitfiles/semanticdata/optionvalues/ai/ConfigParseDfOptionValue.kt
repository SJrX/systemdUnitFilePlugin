package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for VXLAN.IPDoNotFragment
 * C Function: config_parse_df(0)
 * Used by Options: VXLAN.IPDoNotFragment
 * 
 * Validates the IPv4 Do not Fragment (DF) bit setting.
 * Accepts boolean values (yes/no/1/0/true/false/on/off/y/n/t/f) or "inherit".
 */
class ConfigParseDfOptionValue : SimpleGrammarOptionValues(
    "config_parse_df",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("1", "yes", "y", "true", "t", "on", "0", "no", "n", "false", "f", "off", "inherit"),
        EOF()
    )
)
