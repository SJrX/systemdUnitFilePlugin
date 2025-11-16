package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.LLDP
 * C Function: config_parse_lldp_mode(0)
 * Used by Options: Network.LLDP
 * 
 * Accepts boolean values (yes/no/true/false/on/off/1/0) or the special value "routers-only".
 */
class ConfigParseLldpModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_lldp_mode",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal(
            "yes", "y", "true", "t", "on", "1",
            "no", "n", "false", "f", "off", "0",
            "routers-only"
        ),
        EOF()
    )
)
