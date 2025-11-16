package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Bond.ARPIPTargets
 * C Function: config_parse_arp_ip_target_address(0)
 * Used by Options: Bond.ARPIPTargets
 */
class ConfigParseArpIpTargetAddressOptionValue : SimpleGrammarOptionValues(
    "config_parse_arp_ip_target_address",
    SequenceCombinator(
        IPV4_ADDR,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), IPV4_ADDR)),
        EOF()
    )
)
