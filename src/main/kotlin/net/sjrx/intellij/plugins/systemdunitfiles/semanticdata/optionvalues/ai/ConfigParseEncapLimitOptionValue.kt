package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Tunnel.EncapsulationLimit
 * C Function: config_parse_encap_limit(0)
 * Used by Options: Tunnel.EncapsulationLimit
 * 
 * Accepts either "none" or an integer from 0 to 255.
 */
class ConfigParseEncapLimitOptionValue : SimpleGrammarOptionValues(
    "config_parse_encap_limit",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("none"),
            IntegerTerminal(0, 256)  // IntegerTerminal max is exclusive, so 256 means up to 255
        ),
        EOF()
    )
)
