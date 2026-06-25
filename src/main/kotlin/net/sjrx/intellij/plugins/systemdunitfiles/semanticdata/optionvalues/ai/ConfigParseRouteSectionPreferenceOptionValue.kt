package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Route.IPv6Preference: one of low, medium, high (config_parse_route_preference).
 */
class ConfigParseRouteSectionPreferenceOptionValue : SimpleGrammarOptionValues(
    "config_parse_route_section",
    SequenceCombinator(LiteralChoiceTerminal("low", "medium", "high"), EOF())
)
