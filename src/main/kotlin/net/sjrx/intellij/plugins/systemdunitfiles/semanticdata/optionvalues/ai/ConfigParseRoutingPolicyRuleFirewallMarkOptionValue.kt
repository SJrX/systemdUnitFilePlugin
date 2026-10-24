package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CIDR_SEPARATOR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NetworkRouteAddrSafeAtouTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NETWORK_ROUTE_ADDR_UINT32_LIMIT
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [RoutingPolicyRule] FirewallMark= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#FirewallMark=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-routing-policy-rule.c#L1582-L1633   config_parse_routing_policy_rule (ROUTING_POLICY_RULE_FWMARK)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-routing-policy-rule.c#L1426-L1450   config_parse_routing_policy_rule_fwmark
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-routing-policy-rule.c#L1298-L1330   parse_fwmark_fwmask
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478                       safe_atou_full (base 0)
 */

/**
 * Validator for `[RoutingPolicyRule] FirewallMark=` (.network) — the ROUTING_POLICY_RULE_FWMARK slot,
 * whose table entry is config_parse_routing_policy_rule_fwmark → parse_fwmark_fwmask.
 *
 * `MARK` or `MARK/MASK`, split at the first `/`, each read with safe_atou32() — so decimal, `0x` hex,
 * leading-zero / `0o` octal and `0b` binary are all accepted, up to 4294967295. A missing number on
 * either side of the slash is an error.
 */
class ConfigParseRoutingPolicyRuleFirewallMarkOptionValue : SimpleGrammarOptionValues(
    "config_parse_routing_policy_rule",
    SequenceCombinator(
        NetworkRouteAddrSafeAtouTerminal(0, NETWORK_ROUTE_ADDR_UINT32_LIMIT),
        ZeroOrOne(SequenceCombinator(CIDR_SEPARATOR, NetworkRouteAddrSafeAtouTerminal(0, NETWORK_ROUTE_ADDR_UINT32_LIMIT, allowLeadingWhitespace = true))),
        EOF()
    )
)
