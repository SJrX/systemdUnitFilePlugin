package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Unit.AssertFirstBoot, Unit.ConditionFirstBoot
 * C Function: config_parse_unit_condition_string(CONDITION_FIRST_BOOT)
 * Used by Options: Unit.AssertFirstBoot, Unit.ConditionFirstBoot
 * 
 * Accepts boolean values with optional trigger (|) and negate (!) prefixes.
 * Format: [|] [!] <boolean>
 */
class ConfigParseUnitConditionStringOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    SequenceCombinator(
        // Optional trigger prefix: |
        ZeroOrOne(SequenceCombinator(
            LiteralChoiceTerminal("|"),
            ZeroOrMore(WhitespaceTerminal())
        )),
        // Optional negate prefix: !
        ZeroOrOne(SequenceCombinator(
            LiteralChoiceTerminal("!"),
            ZeroOrMore(WhitespaceTerminal())
        )),
        // Boolean value
        BOOLEAN,
        EOF()
    )
)
