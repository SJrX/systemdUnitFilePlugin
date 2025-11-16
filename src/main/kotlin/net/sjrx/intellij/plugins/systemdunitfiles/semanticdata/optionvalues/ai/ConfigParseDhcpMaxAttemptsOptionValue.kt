package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for DHCPv4.MaxAttempts
 * C Function: config_parse_dhcp_max_attempts(0)
 * Used by Options: DHCPv4.MaxAttempts
 */
class ConfigParseDhcpMaxAttemptsOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_max_attempts",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("infinity"),
            IntegerTerminal(1, Long.MAX_VALUE)
        ),
        EOF()
    )
)
