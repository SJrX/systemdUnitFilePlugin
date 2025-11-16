package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for CAKE.UseRawPacketSize
 * C Function: config_parse_cake_tristate(QDISC_KIND_CAKE)
 * Used by Options: CAKE.UseRawPacketSize
 * 
 * Accepts tristate boolean values: yes/no/true/false/on/off/1/0
 */
class ConfigParseCakeTristateOptionValue : SimpleGrammarOptionValues(
    "config_parse_cake_tristate",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("1", "yes", "y", "true", "t", "on", "0", "no", "n", "false", "f", "off"),
        EOF()
    )
)
