package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [DHCPv4] UnassignedSubnetPolicy=, [DHCPv6] UnassignedSubnetPolicy= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#UnassignedSubnetPolicy=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-prefix-delegation.c#L1472-L1518  config_parse_dhcp_pd_prefix_route_type (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-util.c#L341-L356               route_type_table / route_type_from_string
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-util.c#L47-L49                 route_type_is_reject
 */

/**
 * Validator for `UnassignedSubnetPolicy=` (config_parse_dhcp_pd_prefix_route_type).
 *
 * Accepts `none`, or a route type that route_type_is_reject() accepts: `unreachable`, `prohibit`,
 * `blackhole` or `throw`. Other route types (`unicast`, `local`, …) parse but are then refused. No
 * boolean; case-sensitive (streq / string table).
 */
class ConfigParseDhcpPdPrefixRouteTypeOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_pd_prefix_route_type",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("none", "unreachable", "prohibit", "blackhole", "throw"),
        EOF(),
    )
)
