package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ALTERNATIVE_INTERFACE_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IP_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtouTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.UINT32_LIMIT
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [Route] MultiPathRoute= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#MultiPathRoute=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route.c#L1967-L2038           config_parse_route_section (ROUTE_MULTIPATH)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-nexthop.c#L1041-L1142   config_parse_multipath_route
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L112-L124                   parse_ifindex (safe_atoi, > 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L785-L830                  ifname_valid_full (IFNAME_VALID_ALTERNATIVE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L531-L551                 in_addr_from_string_auto
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478                   safe_atou_full (weight, base 0)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/network/test-networkd-conf.c#L352-L440         TEST(config_parse_multipath_route)
 */

private val AT = LiteralChoiceTerminal("@")

// After `@`: parse_ifindex() (a positive int) or, failing that, an alternative-length interface name.
// The name branch comes first because its lenient shape also matches digits but it only *validates*
// non-numeric names; under the classic matcher a number then falls through to the index branch.
private val DEVICE = AlternativeCombinator(ALTERNATIVE_INTERFACE_NAME, SafeAtouTerminal(1, 2_147_483_648L))

/**
 * Validator for `[Route] MultiPathRoute=` (.network) — the ROUTE_MULTIPATH slot, whose table entry is
 * config_parse_multipath_route.
 *
 * The first whitespace-separated word is `[GATEWAY][@DEVICE]` with at least one part present:
 *  - GATEWAY is an IPv4 or IPv6 literal (in_addr_from_string_auto — no prefix, no zone);
 *  - DEVICE, after the first `@`, is an interface index (parse_ifindex: a positive integer) or an
 *    interface name valid with IFNAME_VALID_ALTERNATIVE (up to 127 characters, not all digits).
 * Anything after that word is the weight, read as a whole with safe_atou32() and accepted in 1…256 —
 * a second word, or anything else trailing, is an error.
 *
 * Examples from systemd's own unit test: `@wg0 10`, `@1 15`, `10.0.0.1@eth0 20`,
 * `2001:db8::1@wg0 15`, `192.168.1.1 100`, `fe80::1`, `@999999 10`.
 */
class ConfigParseRouteMultiPathOptionValue : SimpleGrammarOptionValues(
    "config_parse_route_section",
    SequenceCombinator(
        AlternativeCombinator(
            SequenceCombinator(IP_ADDR, ZeroOrOne(SequenceCombinator(AT, DEVICE))),
            SequenceCombinator(AT, DEVICE),
        ),
        ZeroOrOne(SequenceCombinator(WhitespaceTerminal(), SafeAtouTerminal(1, 257))),
        EOF()
    )
)
