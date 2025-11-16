package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Bridge.MulticastIGMPVersion
 * C Function: config_parse_bridge_igmp_version(0)
 * Used by Options: Bridge.MulticastIGMPVersion
 */
class ConfigParseBridgeIgmpVersionOptionValue : SimpleGrammarOptionValues(
    "config_parse_bridge_igmp_version",
    SequenceCombinator(
        IntegerTerminal(2, 4),  // Accepts 2 or 3 (upper bound is exclusive)
        EOF()
    )
)
