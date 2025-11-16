package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Bridge.MulticastRouter
 * C Function: config_parse_multicast_router(0)
 * Used by Options: Bridge.MulticastRouter
 */
class ConfigParseMulticastRouterOptionValue : SimpleGrammarOptionValues(
    "config_parse_multicast_router",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("no", "query", "permanent", "temporary"),
        EOF()
    )
)
