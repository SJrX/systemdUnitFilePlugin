package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.IPv6PrivacyExtensions
 * C Function: config_parse_ipv6_privacy_extensions(0)
 * Used by Options: Network.IPv6PrivacyExtensions
 * 
 * Accepts boolean values (yes/no/true/false/on/off/1/0/y/n/t/f) or the special values
 * "prefer-public" and "kernel".
 */
class ConfigParseIpv6PrivacyExtensionsOptionValue : SimpleGrammarOptionValues(
    "config_parse_ipv6_privacy_extensions",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal(
            // Boolean values
            "1", "yes", "y", "true", "t", "on",
            "0", "no", "n", "false", "f", "off",
            // Special values
            "prefer-public", "kernel"
        ),
        EOF()
    )
)
