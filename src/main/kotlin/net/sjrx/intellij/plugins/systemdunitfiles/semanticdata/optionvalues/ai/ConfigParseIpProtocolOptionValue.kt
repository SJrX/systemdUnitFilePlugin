package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for FooOverUDP.Protocol
 * C Function: config_parse_ip_protocol(true)
 * Used by Options: FooOverUDP.Protocol
 *
 * Accepts an IP protocol name from /etc/protocols (e.g. tcp, udp, icmp) or an integer 0..255.
 * The list of protocol names below is intentionally conservative; rare names will produce a
 * (false-positive) warning, which is acceptable.
 */
class ConfigParseIpProtocolOptionValue : SimpleGrammarOptionValues(
    "config_parse_ip_protocol",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("tcp", "udp", "icmp", "icmpv6", "sctp", "udplite", "dccp"),
            IntegerTerminal(0, 256)
        ),
        EOF()
    )
)
