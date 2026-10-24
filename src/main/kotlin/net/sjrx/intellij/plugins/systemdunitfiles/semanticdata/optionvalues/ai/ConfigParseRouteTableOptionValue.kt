package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NetworkRouteAddrSafeAtouTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NETWORK_ROUTE_ADDR_UINT32_LIMIT

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

// A route table *name*: `default`, `main`, `local`, or one defined with RouteTable=NAME:NUMBER in
// networkd.conf. config_parse_route_table_names splits RouteTable= on whitespace and on the first `:`,
// and refuses an all-digit name — so a name has no whitespace or `:` and is not purely digits.
private val ROUTE_TABLE_NAME = RegexTerminal("""\S+""", """(?![0-9]+(?:\s|$))[^\s:]+""")

/**
 * Validator for `[Route] Table=` and `[RoutingPolicyRule] Table=` (.network). Both table slots end in
 * manager_get_route_table_from_string(), so they share this grammar.
 *
 * That tries, in order: the predefined names `default`, `main`, `local`; the names defined by
 * `RouteTable=` in networkd.conf; and finally safe_atou32(), refusing 0. The user-defined names live in
 * a different file, so any well-formed name is accepted (a name cannot contain whitespace or `:` and
 * cannot be all digits). An all-digit value must be a table number in 1…4294967295; `0`, an
 * out-of-range number, or a value containing whitespace or `:` is flagged.
 *
 * Lenient corner: a non-decimal number such as `0x0` is indistinguishable from a possible user-defined
 * name and is accepted.
 */
class ConfigParseRouteTableOptionValue(parserName: String = "config_parse_route_section") : SimpleGrammarOptionValues(
    parserName,
    SequenceCombinator(
        // The name branch must come first: its lenient shape matches digits too but only *validates*
        // non-numeric names, so under the classic matcher a number falls through to the second branch.
        AlternativeCombinator(ROUTE_TABLE_NAME, NetworkRouteAddrSafeAtouTerminal(1, NETWORK_ROUTE_ADDR_UINT32_LIMIT)),
        EOF()
    )
)
