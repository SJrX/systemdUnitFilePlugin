package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.IPv4ReversePathFilter
 * C Function: config_parse_ip_reverse_path_filter(0)
 * Used by Options: Network.IPv4ReversePathFilter
 */
class ConfigParseIpReversePathFilterOptionValue : SimpleGrammarOptionValues(
    "config_parse_ip_reverse_path_filter",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("no", "strict", "loose"),
        EOF()
    )
)
