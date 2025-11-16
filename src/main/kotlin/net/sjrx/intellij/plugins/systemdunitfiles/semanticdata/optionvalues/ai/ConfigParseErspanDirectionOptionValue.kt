package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Tunnel.ERSPANDirection
 * C Function: config_parse_erspan_direction(0)
 * Used by Options: Tunnel.ERSPANDirection
 */
class ConfigParseErspanDirectionOptionValue : SimpleGrammarOptionValues(
    "config_parse_erspan_direction",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("ingress", "egress"),
        EOF()
    )
)
