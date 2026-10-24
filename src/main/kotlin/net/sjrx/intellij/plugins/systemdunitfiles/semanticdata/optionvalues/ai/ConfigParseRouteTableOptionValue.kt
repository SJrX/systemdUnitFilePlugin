package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ROUTE_TABLE_FROM_STRING

/*
 * [Route] Table= and [RoutingPolicyRule] Table= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Table=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route.c#L1967-L2038                 config_parse_route_section (ROUTE_TABLE)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route.c#L1862-L1886                 config_parse_route_table
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-routing-policy-rule.c#L1582-L1633   config_parse_routing_policy_rule (ROUTING_POLICY_RULE_TABLE)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-routing-policy-rule.c#L1397-L1424   config_parse_routing_policy_rule_table
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-util.c#L435-L464   manager_get_route_table_from_string
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-util.c#L496-L616   config_parse_route_table_names (what a user-defined name may look like)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478             safe_atou_full (base 0)
 */

/**
 * Validator for `[Route] Table=` and `[RoutingPolicyRule] Table=` (.network). Both table slots hand the whole
 * value to manager_get_route_table_from_string(), so they use the shared [ROUTE_TABLE_FROM_STRING] grammar.
 *
 * That tries, in order: the predefined names `default`, `main`, `local`; the names defined by `RouteTable=` in
 * networkd.conf; and finally safe_atou32(), refusing 0. The user-defined names live in a file the plugin cannot
 * see, so every value that could be such a name is accepted: anything non-empty without a `:` that is not all
 * digits. That includes `0x0`, `-0`, and names with whitespace such as `main table` (networkd.conf can define
 * `main\ table:100`). An all-digit value must be a table number in 1…4294967295, so `0`, `00`, `08` and
 * `4294967296` are flagged, as is any value containing `:`.
 */
class ConfigParseRouteTableOptionValue(parserName: String = "config_parse_route_section") : SimpleGrammarOptionValues(
    parserName,
    ROUTE_TABLE_FROM_STRING,
)
