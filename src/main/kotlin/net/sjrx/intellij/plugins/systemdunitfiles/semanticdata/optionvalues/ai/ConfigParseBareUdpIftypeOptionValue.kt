package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for BareUDP.EtherType
 * C Function: config_parse_bare_udp_iftype(0)
 * Used by Options: BareUDP.EtherType
 * 
 * Validates the L3 protocol for BareUDP interfaces.
 * Valid values: ipv4, ipv6, mpls-uc, mpls-mc
 */
class ConfigParseBareUdpIftypeOptionValue : SimpleGrammarOptionValues(
    "config_parse_bare_udp_iftype",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("ipv4", "ipv6", "mpls-uc", "mpls-mc"),
        EOF()
    )
)
