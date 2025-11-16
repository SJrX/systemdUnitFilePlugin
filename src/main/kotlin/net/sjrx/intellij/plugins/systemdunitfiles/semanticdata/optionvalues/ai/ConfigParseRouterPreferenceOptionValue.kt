package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for IPv6SendRA.RouterPreference
 * C Function: config_parse_router_preference(0)
 * Used by Options: IPv6SendRA.RouterPreference
 */
class ConfigParseRouterPreferenceOptionValue : SimpleGrammarOptionValues(
    "config_parse_router_preference",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("high", "medium", "normal", "default", "low"),
        EOF()
    )
)
