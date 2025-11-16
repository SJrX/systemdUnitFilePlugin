package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.MulticastIGMPVersion
 * C Function: config_parse_ipv4_force_igmp_version(0)
 * Used by Options: Network.MulticastIGMPVersion
 * 
 * Validates IPv4 Multicast IGMP Version values.
 * Valid values: no, v1, v2, v3
 */
class ConfigParseIpv4ForceIgmpVersionOptionValue : SimpleGrammarOptionValues(
    "config_parse_ipv4_force_igmp_version",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("no", "v1", "v2", "v3"),
        EOF()
    )
)
