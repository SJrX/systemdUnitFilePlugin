package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Tunnel.ERSPANHardwareId
 * C Function: config_parse_erspan_hwid(0)
 * Used by Options: Tunnel.ERSPANHardwareId
 */
class ConfigParseErspanHwidOptionValue : SimpleGrammarOptionValues(
    "config_parse_erspan_hwid",
    SequenceCombinator(
        IntegerTerminal(0, 64),
        EOF()
    )
)
