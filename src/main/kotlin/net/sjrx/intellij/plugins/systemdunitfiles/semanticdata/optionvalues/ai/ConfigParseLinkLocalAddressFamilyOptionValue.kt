package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.LinkLocalAddressing
 * C Function: config_parse_link_local_address_family(0)
 * Used by Options: Network.LinkLocalAddressing
 */
class ConfigParseLinkLocalAddressFamilyOptionValue : SimpleGrammarOptionValues(
    "config_parse_link_local_address_family",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal(
            "yes", "no", "true", "false", "1", "0", "on", "off", "y", "n", "t", "f",
            "ipv4", "ipv6",
            "fallback", "fallback-ipv4"
        ),
        EOF()
    )
)
