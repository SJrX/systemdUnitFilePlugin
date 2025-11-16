package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.UseDomains
 * C Function: config_parse_use_domains(0)
 * Used by Options: Network.UseDomains
 * 
 * Accepts boolean values (yes/no/true/false/1/0/on/off/y/n/t/f) or the special value "route".
 */
class ConfigParseUseDomainsOptionValue : SimpleGrammarOptionValues(
    "config_parse_use_domains",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("yes", "no", "true", "false", "1", "0", "on", "off", "y", "n", "t", "f", "route"),
        EOF()
    )
)
