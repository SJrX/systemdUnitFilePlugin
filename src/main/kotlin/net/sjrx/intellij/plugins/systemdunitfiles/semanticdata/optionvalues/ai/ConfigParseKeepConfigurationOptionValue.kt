package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.KeepConfiguration
 * C Function: config_parse_keep_configuration(0)
 * Used by Options: Network.KeepConfiguration
 * 
 * Accepts:
 * - Boolean values: 1, yes, y, true, t, on, 0, no, n, false, f, off
 * - Specific values: static, dynamic-on-stop, dynamic
 * - Backward compatibility: dhcp, dhcp-on-stop
 */
class ConfigParseKeepConfigurationOptionValue : SimpleGrammarOptionValues(
    "config_parse_keep_configuration",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal(
            // Boolean values (map to yes/no)
            "1", "yes", "y", "true", "t", "on",
            "0", "no", "n", "false", "f", "off",
            // Explicit enum values
            "static", "dynamic-on-stop", "dynamic",
            // Backward compatibility
            "dhcp", "dhcp-on-stop"
        ),
        EOF()
    )
)
