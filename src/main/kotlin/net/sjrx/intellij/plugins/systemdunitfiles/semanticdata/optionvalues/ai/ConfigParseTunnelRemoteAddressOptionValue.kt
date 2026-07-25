package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV4_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV6_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Tunnel] Remote= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#Remote=
 * parser https://github.com/systemd/systemd/blob/a8e93919c3/src/network/netdev/tunnel.c  config_parse_tunnel_remote_address
 */

/**
 * Validator for `[Tunnel] Remote=` (.netdev).
 *
 * C function: config_parse_tunnel_remote_address in src/network/netdev/tunnel.c — `any` (unset) or an
 * IPv4/IPv6 literal via in_addr_from_string_auto. Unlike `Local=` it has no local-address-type
 * keywords: those name a way for the *host* to obtain its own address, which says nothing about the
 * far end of the tunnel.
 */
class ConfigParseTunnelRemoteAddressOptionValue : SimpleGrammarOptionValues(
    "config_parse_tunnel_remote_address",
    SequenceCombinator(
        AlternativeCombinator(
            IPV6_ADDR,
            IPV4_ADDR,
            LiteralChoiceTerminal("any"),
        ),
        EOF()
    )
)
