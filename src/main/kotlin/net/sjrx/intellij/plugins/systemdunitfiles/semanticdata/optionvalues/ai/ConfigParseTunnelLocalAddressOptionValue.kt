package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV4_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV6_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/**
 * Validator for `[Tunnel] Local=` (.netdev).
 *
 * C function: config_parse_tunnel_local_address in src/network/netdev/tunnel.c. It accepts `any`
 * (unset), one of the netdev_local_address_type_table names — which tell networkd to pick up the
 * address assigned by that mechanism — or an IPv4/IPv6 literal via in_addr_from_string_auto.
 *
 * The address alternatives come before the keyword terminal: the keyword terminal matches loosely by
 * character shape, so on `10.65.0.1` it would otherwise consume the leading `10` and strand the rest
 * (the classic matcher commits to the first alternative that matches and never backtracks).
 */
class ConfigParseTunnelLocalAddressOptionValue : SimpleGrammarOptionValues(
    "config_parse_tunnel_local_address",
    SequenceCombinator(
        AlternativeCombinator(
            IPV6_ADDR,
            IPV4_ADDR,
            FlexibleLiteralChoiceTerminal(
                "any",
                "ipv4_link_local",
                "ipv6_link_local",
                "dhcp4",
                "dhcp6",
                "slaac",
                "dhcp_pd",
            ),
        ),
        EOF()
    )
)
