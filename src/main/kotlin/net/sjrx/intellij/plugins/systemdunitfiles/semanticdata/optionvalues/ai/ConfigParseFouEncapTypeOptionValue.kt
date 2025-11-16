package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for FooOverUDP.Encapsulation
 * C Function: config_parse_fou_encap_type(0)
 * Used by Options: FooOverUDP.Encapsulation
 */
class ConfigParseFouEncapTypeOptionValue : SimpleGrammarOptionValues(
    "config_parse_fou_encap_type",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("FooOverUDP", "GenericUDPEncapsulation"),
        EOF()
    )
)
