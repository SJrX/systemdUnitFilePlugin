package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Tunnel.ERSPANVersion
 * C Function: config_parse_erspan_version(0)
 * Used by Options: Tunnel.ERSPANVersion
 */
class ConfigParseErspanVersionOptionValue : SimpleGrammarOptionValues(
    "config_parse_erspan_version",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("0", "1", "2"),
        EOF()
    )
)
