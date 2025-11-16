package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for DHCPv4.IAID
 * C Function: config_parse_iaid(AF_INET)
 * Used by Options: DHCPv4.IAID
 * 
 * Validates a 32-bit unsigned integer (0 to 4,294,967,295).
 */
class ConfigParseIaidOptionValue : SimpleGrammarOptionValues(
    "config_parse_iaid",
    SequenceCombinator(
        IntegerTerminal(0, 4294967296), // 32-bit unsigned: 0 to 2^32-1 (max is exclusive)
        EOF()
    )
)
