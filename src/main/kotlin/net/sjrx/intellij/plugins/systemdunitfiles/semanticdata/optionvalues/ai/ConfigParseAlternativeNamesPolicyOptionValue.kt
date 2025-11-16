package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Link.AlternativeNamesPolicy
 * C Function: config_parse_alternative_names_policy(0)
 * Used by Options: Link.AlternativeNamesPolicy
 */
class ConfigParseAlternativeNamesPolicyOptionValue : SimpleGrammarOptionValues(
    "config_parse_alternative_names_policy",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("database", "onboard", "slot", "path", "mac"),
        ZeroOrMore(
            SequenceCombinator(
                WhitespaceTerminal(),
                FlexibleLiteralChoiceTerminal("database", "onboard", "slot", "path", "mac")
            )
        ),
        EOF()
    )
)
