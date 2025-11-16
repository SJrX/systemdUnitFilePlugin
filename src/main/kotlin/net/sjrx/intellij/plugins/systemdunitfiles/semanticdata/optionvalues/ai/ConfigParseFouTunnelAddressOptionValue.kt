package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for FooOverUDP.Peer
 * C Function: config_parse_fou_tunnel_address(0)
 * Used by Options: FooOverUDP.Peer
 * 
 * Validates peer IP address for FooOverUDP tunnel.
 * Accepts both IPv4 and IPv6 addresses.
 */
class ConfigParseFouTunnelAddressOptionValue : SimpleGrammarOptionValues(
    "config_parse_fou_tunnel_address",
    SequenceCombinator(
        AlternativeCombinator(
            IPV4_ADDR,
            IPV6_ADDR
        ),
        EOF()
    )
)
