package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for IPv6RoutePrefix.Preference
 * C Function: config_parse_route_prefix_preference(0)
 * Used by Options: IPv6RoutePrefix.Preference
 */
class ConfigParseRoutePrefixPreferenceOptionValue : SimpleGrammarOptionValues(
    "config_parse_route_prefix_preference",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("high", "medium", "low"),
        EOF()
    )
)
