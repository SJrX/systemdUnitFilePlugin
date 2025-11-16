package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Bond.AdUserPortKey
 * C Function: config_parse_ad_user_port_key(0)
 * Used by Options: Bond.AdUserPortKey
 * 
 * Validates the 802.3ad user defined portion of the port key.
 * Accepts a number in the range 0–1023.
 */
class ConfigParseAdUserPortKeyOptionValue : SimpleGrammarOptionValues(
    "config_parse_ad_user_port_key",
    SequenceCombinator(
        IntegerTerminal(0, 1024),  // Range 0-1023 (maxExclusive is 1024)
        EOF()
    )
)
