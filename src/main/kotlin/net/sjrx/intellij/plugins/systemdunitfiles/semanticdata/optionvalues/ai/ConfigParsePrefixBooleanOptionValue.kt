package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for IPv6Prefix.OnLink
 * C Function: config_parse_prefix_boolean(ND_OPT_PI_FLAG_ONLINK)
 * Used by Options: IPv6Prefix.OnLink
 * 
 * Validates boolean values for IPv6 prefix configuration options.
 * The C implementation uses parse_boolean() which accepts standard boolean values.
 */
class ConfigParsePrefixBooleanOptionValue : SimpleGrammarOptionValues(
    "config_parse_prefix_boolean",
    SequenceCombinator(
        BOOLEAN,
        EOF()
    )
)
