package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN_FALSE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ROUTE_TABLE_FROM_STRING
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [WireGuardPeer] RouteTable= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#RouteTable=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/wireguard.c#L941-L990   config_parse_wireguard_peer_route_table (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-util.c#L435-L464   manager_get_route_table_from_string
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-util.c#L427-L431   route_table_table (default, main, local)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-util.c#L496-L616   config_parse_route_table_names (what a user-defined name may be)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-wireguard.netdev#L10
 */

/**
 * Validator for `[WireGuardPeer] RouteTable=` (.netdev).
 *
 * C function: config_parse_wireguard_peer_route_table. A value that parse_boolean() reads as false (`no`, `off`, `0`, …)
 * disables route configuration; anything else is a route table for manager_get_route_table_from_string:
 * `default`, `main`, `local`, a name defined with `RouteTable=` in networkd.conf, or a non-zero uint32
 * (safe_atou32, so hex, octal and `0b`/`0o` work). See [ROUTE_TABLE_FROM_STRING]. An empty assignment falls back to the `[WireGuard]` section's table.
 *
 * Because table names are user-defined (networkd.conf can even define `my\ table:100`), any value without
 * `:` that is not all digits is accepted as a potential name — including `yes`, which is only valid if
 * networkd.conf defines it. What is flagged: out-of-range or malformed all-digit numbers (`4294967296`,
 * `08`) and anything containing `:` (`name:number` is networkd.conf syntax, not a table reference). An
 * invalid value is logged and ignored by systemd.
 */
class ConfigParseWireguardPeerRouteTableOptionValue : SimpleGrammarOptionValues(
    "config_parse_wireguard_peer_route_table",
    AlternativeCombinator(
        SequenceCombinator(BOOLEAN_FALSE, EOF()),
        ROUTE_TABLE_FROM_STRING,
    )
)
