package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ROUTE_TABLE_FROM_STRING

/*
 * [DHCPv4] RouteTable=, [DHCPv6] RouteTable=, [IPv6AcceptRA] RouteTable= (and the old [DHCP] RouteTable=) (.network).
 *
 * Note: the plugin reports the deprecated [DHCP] section as not allowed in .network files, so this validator never
 * actually runs for the key there; it is listed only because the gperf shares the parser.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#RouteTable=num
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L560-L606  config_parse_dhcp_or_ra_route_table (NETWORK_CONFIG_SOURCE_DHCP4 / _DHCP6 / _NDISC)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-util.c#L435-L464   manager_get_route_table_from_string
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-util.c#L496-L616   config_parse_route_table_names (what a user-defined name may be)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478              safe_atou_full (safe_atou32, base 0)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-dhcp-client-ipv4-only.network#L17
 */

/**
 * Validator for `RouteTable=` in [DHCPv4], [DHCPv6] and [IPv6AcceptRA] (config_parse_dhcp_or_ra_route_table). The
 * ltype only picks which Network field receives the table. The parser hands the whole value to
 * manager_get_route_table_from_string() and adds nothing of its own: no special empty value and no extra bounds.
 * So the grammar is the shared [ROUTE_TABLE_FROM_STRING]: `default`, `main` or `local` (case-sensitive), a table
 * number 1…4294967295 read by safe_atou32 (so `0x10`, `010` and `0b 1` are numbers too, and `0` is refused), or
 * anything networkd.conf could have defined as a table name (non-empty, no `:`, not all digits).
 *
 * The name branch is lenient by design: networkd.conf is not visible to the plugin, so `mytable` is accepted
 * although systemd refuses it when networkd.conf does not define it. What is flagged is what systemd refuses
 * whatever networkd.conf says: `0`, `00`, `08`, `4294967296` and anything containing `:` (checked with
 * systemd's parser at 0f0682b773 on all three keys).
 */
class ConfigParseDhcpOrRaRouteTableOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_or_ra_route_table",
    ROUTE_TABLE_FROM_STRING,
)
