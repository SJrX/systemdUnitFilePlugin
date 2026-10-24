package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Checked
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtouTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.safeAtouValue

/*
 * [RoutingPolicyRule] IPProtocol= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#IPProtocol=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-routing-policy-rule.c#L1582-L1633   config_parse_routing_policy_rule (ROUTING_POLICY_RULE_IP_PROTOCOL)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L2257-L2287                    config_parse_ip_protocol (ltype 0 = not relaxed)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/ip-protocol-list.c#L36-L70                   parse_ip_protocol_full
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/generate-ip-protocol-list.sh                 every `#define IPPROTO_*` cpp sees for <netinet/in.h>
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/meson.build#L328-L333                        ... run with system_include_args, so <netinet/in.h> is systemd's override
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/include/override/netinet/in.h#L4-L6                 which includes the bundled uapi <linux/in.h>, <linux/in6.h>
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/include/uapi/linux/in.h#L27-L92                     IPPROTO_IP ... IPPROTO_MPTCP (incl. AGGFRAG = 144)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/include/uapi/linux/in6.h#L131-L139                  IPPROTO_HOPOPTS ... IPPROTO_MH
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/ip-protocol-to-name.awk                      number -> name table (HOPOPTS skipped; 0 is "ip")
 * linux  https://man7.org/linux/man-pages/man5/protocols.5.html
 */

/**
 * The IPPROTO_* names the generated gperf table knows, lower-cased. The table is generated from
 * systemd's own vendored uapi headers (the override <netinet/in.h> includes <linux/in.h> and
 * <linux/in6.h>, not glibc's), so it is fixed per systemd commit. `smc` (256) and `mptcp` (262) are left
 * out because config_parse_ip_protocol refuses anything above 255.
 */
private val IP_PROTOCOL_NAMES = arrayOf(
    "ip", "hopopts", "icmp", "igmp", "ipip", "tcp", "egp", "pup", "udp", "idp", "tp", "dccp", "ipv6",
    "routing", "fragment", "rsvp", "gre", "esp", "ah", "icmpv6", "none", "dstopts", "mtp", "beetph",
    "encap", "pim", "comp", "l2tp", "mh", "sctp", "udplite", "mpls", "ethernet", "aggfrag", "raw",
)

/** The protocol numbers ip_protocol_to_name() has a name for (and that fit in a byte). */
private val IP_PROTOCOL_NUMBERS = setOf(
    0, 1, 2, 4, 6, 8, 12, 17, 22, 29, 33, 41, 43, 44, 46, 47, 50, 51, 58, 59, 60, 92, 94, 98, 103, 108, 115,
    132, 135, 136, 137, 143, 144, 255,
)

/**
 * Validator for `[RoutingPolicyRule] IPProtocol=` (.network) — the ROUTING_POLICY_RULE_IP_PROTOCOL slot,
 * whose table entry is config_parse_ip_protocol with ltype 0.
 *
 * parse_ip_protocol_full(…, relaxed = false) accepts an IPPROTO_* name looked up case-insensitively
 * (the gperf table is built with `--ignore-case`, and the value is also retried lower-cased), or a
 * number read with safe_atoi() (base 0, so `0x11` works) that ip_protocol_to_name() has a name for
 * (otherwise -EPROTONOSUPPORT; e.g. `3` is refused). config_parse_ip_protocol then refuses anything
 * above 255, because the netlink attribute is one byte.
 */
class ConfigParseRoutingPolicyRuleIpProtocolOptionValue : SimpleGrammarOptionValues(
    "config_parse_routing_policy_rule",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal(*IP_PROTOCOL_NAMES, ignoreCase = true),
            Checked(SafeAtouTerminal(0, 256)) { text -> safeAtouValue(text)?.toInt() in IP_PROTOCOL_NUMBERS },
        ),
        EOF()
    )
)
