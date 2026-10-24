package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtouTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [RoutingPolicyRule] IPProtocol= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#IPProtocol=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-routing-policy-rule.c#L1582-L1633   config_parse_routing_policy_rule (ROUTING_POLICY_RULE_IP_PROTOCOL)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L2257-L2287                    config_parse_ip_protocol (ltype 0 = not relaxed)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/ip-protocol-list.c#L36-L70                   parse_ip_protocol_full
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/generate-ip-protocol-list.sh                 IPPROTO_* names from <netinet/in.h>
 * linux  https://man7.org/linux/man-pages/man5/protocols.5.html
 */

/**
 * The IPPROTO_* names the generated gperf table knows, lower-cased, as defined by glibc's
 * <netinet/in.h> (generate-ip-protocol-list.sh lists every `#define IPPROTO_*`). `smc` (256) and
 * `mptcp` (262) are left out because config_parse_ip_protocol refuses anything above 255.
 */
private val IP_PROTOCOL_NAMES = arrayOf(
    "ip", "hopopts", "icmp", "igmp", "ipip", "tcp", "egp", "pup", "udp", "idp", "tp", "dccp", "ipv6",
    "routing", "fragment", "rsvp", "gre", "esp", "ah", "icmpv6", "none", "dstopts", "mtp", "beetph",
    "encap", "pim", "comp", "l2tp", "mh", "sctp", "udplite", "mpls", "ethernet", "raw",
)

/**
 * Validator for `[RoutingPolicyRule] IPProtocol=` (.network) — the ROUTING_POLICY_RULE_IP_PROTOCOL slot,
 * whose table entry is config_parse_ip_protocol with ltype 0.
 *
 * parse_ip_protocol_full(…, relaxed = false) accepts an IPPROTO_* name looked up case-insensitively
 * (the gperf table is built with `--ignore-case`, and the value is also retried lower-cased), or a
 * number read with safe_atoi() (base 0, so `0x11` works). config_parse_ip_protocol then refuses
 * anything above 255, because the netlink attribute is one byte.
 *
 * Deliberately lenient: with relaxed = false systemd also refuses a number that has no IPPROTO_* name
 * (e.g. `3`), but the name list comes from the C library systemd was built against, so every number in
 * 0…255 is accepted here rather than guess at it.
 */
class ConfigParseRoutingPolicyRuleIpProtocolOptionValue : SimpleGrammarOptionValues(
    "config_parse_routing_policy_rule",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal(*IP_PROTOCOL_NAMES, ignoreCase = true),
            SafeAtouTerminal(0, 256),
        ),
        EOF()
    )
)
