package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Bridge.Priority
 * C Function: config_parse_bridge_port_priority(0)
 * Used by Options: Bridge.Priority
 */
class ConfigParseBridgePortPriorityOptionValue : SimpleGrammarOptionValues(
    "config_parse_bridge_port_priority",
    SequenceCombinator(
        IntegerTerminal(0, 64),  // Range 0-63 inclusive (maxExclusive = 64)
        EOF()
    )
)
