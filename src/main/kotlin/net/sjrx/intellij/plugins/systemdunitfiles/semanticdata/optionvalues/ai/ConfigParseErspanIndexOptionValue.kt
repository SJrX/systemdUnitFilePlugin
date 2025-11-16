package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Tunnel.ERSPANIndex
 * C Function: config_parse_erspan_index(0)
 * Used by Options: Tunnel.ERSPANIndex
 * 
 * Validates ERSPAN v1 index field values in the range 0-1048575.
 */
class ConfigParseErspanIndexOptionValue : SimpleGrammarOptionValues(
    "config_parse_erspan_index",
    SequenceCombinator(
        IntegerTerminal(0, 1048576),  // 0 to 1048575 inclusive (max is exclusive)
        EOF()
    )
)
