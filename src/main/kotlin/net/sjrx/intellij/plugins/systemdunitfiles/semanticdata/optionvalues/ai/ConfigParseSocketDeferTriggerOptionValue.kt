package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Socket.DeferTrigger
 * C Function: config_parse_socket_defer_trigger(0)
 * Used by Options: Socket.DeferTrigger
 */
class ConfigParseSocketDeferTriggerOptionValue : SimpleGrammarOptionValues(
    "config_parse_socket_defer_trigger",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("patient", "yes", "y", "true", "t", "on", "1", "no", "n", "false", "f", "off", "0"),
        EOF()
    )
)
