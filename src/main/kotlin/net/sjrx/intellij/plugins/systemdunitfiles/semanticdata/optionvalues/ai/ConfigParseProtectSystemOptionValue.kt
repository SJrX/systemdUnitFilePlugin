package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Swap.ProtectSystem
 * C Function: config_parse_protect_system(0)
 * Used by Options: Swap.ProtectSystem
 * 
 * Accepts boolean values or the special values "full" or "strict".
 */
class ConfigParseProtectSystemOptionValue : SimpleGrammarOptionValues(
    "config_parse_protect_system",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal(
            "1", "yes", "y", "true", "t", "on",
            "0", "no", "n", "false", "f", "off",
            "full", "strict"
        ),
        EOF()
    )
)
