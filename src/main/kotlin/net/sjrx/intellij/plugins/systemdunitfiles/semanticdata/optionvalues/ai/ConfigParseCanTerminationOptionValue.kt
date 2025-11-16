package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for CAN.Termination
 * C Function: config_parse_can_termination(0)
 * Used by Options: CAN.Termination
 * 
 * Accepts:
 * - Boolean values (yes/no/true/false/on/off/1/0)
 * - Integer values from 0 to 65535 (ohm values)
 * 
 * When a boolean true value is provided, it defaults to 120 ohm.
 * When false or 0, termination is disabled.
 */
class ConfigParseCanTerminationOptionValue : SimpleGrammarOptionValues(
    "config_parse_can_termination",
    SequenceCombinator(
        AlternativeCombinator(
            IntegerTerminal(0, 65536),  // 0-65535 inclusive (max is exclusive)
            BOOLEAN
        ),
        EOF()
    )
)
