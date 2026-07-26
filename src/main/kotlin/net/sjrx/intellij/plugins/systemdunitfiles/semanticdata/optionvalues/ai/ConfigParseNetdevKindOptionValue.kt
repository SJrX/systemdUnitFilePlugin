package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [NetDev] Kind= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#Kind=
 * parser https://github.com/systemd/systemd/blob/a8e93919c3/src/network/netdev/netdev.c  config_parse_netdev_kind
 * values https://github.com/systemd/systemd/blob/a8e93919c3/src/network/netdev/netdev.c  netdev_kind_table
 */

/**
 * Validator for `[NetDev] Kind=` (.netdev).
 *
 * C function: config_parse_netdev_kind in src/network/netdev/netdev.c — a single name resolved by
 * netdev_kind_from_string against netdev_kind_table. Not a list.
 */
class ConfigParseNetdevKindOptionValue : SimpleGrammarOptionValues(
    "config_parse_netdev_kind",
    SequenceCombinator(NETDEV_KIND, EOF())
) {
    companion object {
        private val NETDEV_KIND = FlexibleLiteralChoiceTerminal(
            "bareudp",
            "batadv",
            "bond",
            "bridge",
            "dummy",
            "erspan",
            "fou",
            "geneve",
            "gre",
            "gretap",
            "hsr",
            "ifb",
            "ip6gre",
            "ip6gretap",
            "ip6tnl",
            "ipip",
            "ipoib",
            "ipvlan",
            "ipvtap",
            "l2tp",
            "macsec",
            "macvlan",
            "macvtap",
            "nlmon",
            "sit",
            "tap",
            "tun",
            "vcan",
            "veth",
            "vlan",
            "vrf",
            "vti",
            "vti6",
            "vxcan",
            "vxlan",
            "wireguard",
            "wlan",
            "xfrm",
        )
    }
}
