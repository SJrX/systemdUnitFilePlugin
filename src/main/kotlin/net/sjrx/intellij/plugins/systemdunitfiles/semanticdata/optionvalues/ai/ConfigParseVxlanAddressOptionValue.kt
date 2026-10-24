package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV4_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV6_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [VXLAN] Group= / Local= / Remote= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#Local=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/vxlan.c#L220-L297   config_parse_vxlan_address (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/netdev-util.c#L9-L16   netdev_local_address_type_table
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L531-L551   in_addr_from_string_auto
 * linux  https://man7.org/linux/man-pages/man3/inet_pton.3.html
 */

/**
 * Validator for `[VXLAN] Group=`, `Local=` and `Remote=` (.netdev).
 *
 * C function: config_parse_vxlan_address. The value is an IPv4 or IPv6 literal
 * (in_addr_from_string_auto → inet_pton). For `Local=` only, one of the netdev_local_address_type_table
 * names (`ipv4_link_local`, `ipv6_link_local`, `dhcp4`, `dhcp6`, `slaac`, `dhcp_pd`) is accepted
 * first, telling networkd to pick up the address that mechanism assigned.
 *
 * One grammar serves all three keys because validators are keyed by (parser, ltype), not by key, so
 * this is deliberately lenient in two ways that depend on the key name: the local-address type names
 * are accepted for `Group=`/`Remote=` too, and the multicast checks (`Group=` must be multicast,
 * `Local=`/`Remote=` must not be) are not modelled. Addresses come before the names so the
 * name terminal's loose shape cannot swallow a leading fragment of an address.
 */
class ConfigParseVxlanAddressOptionValue : SimpleGrammarOptionValues(
    "config_parse_vxlan_address",
    SequenceCombinator(
        AlternativeCombinator(
            IPV6_ADDR,
            IPV4_ADDR,
            FlexibleLiteralChoiceTerminal("ipv4_link_local", "ipv6_link_local", "dhcp4", "dhcp6", "slaac", "dhcp_pd"),
        ),
        EOF()
    )
)
