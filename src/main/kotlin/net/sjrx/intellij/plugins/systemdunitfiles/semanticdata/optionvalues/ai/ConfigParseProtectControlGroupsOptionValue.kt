package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Swap.ProtectControlGroups
 * C Function: config_parse_protect_control_groups(0)
 * Used by Options: Swap.ProtectControlGroups
 * 
 * Accepts boolean values (true/false/yes/no/1/0/on/off/y/n/t/f) or special values "private" and "strict".
 */
class ConfigParseProtectControlGroupsOptionValue : SimpleGrammarOptionValues(
    "config_parse_protect_control_groups",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal(
            "1", "yes", "y", "true", "t", "on",
            "0", "no", "n", "false", "f", "off",
            "private", "strict"
        ),
        EOF()
    )
)
