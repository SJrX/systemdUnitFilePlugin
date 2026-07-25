package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ByteSizeTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * MTUBytes= and its family-specific siblings.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.link.html#MTUBytes=
 *        https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#IPv6MTUBytes=
 * parser https://github.com/systemd/systemd/blob/a8e93919c3/src/shared/conf-parser.c  config_parse_mtu
 *        https://github.com/systemd/systemd/blob/a8e93919c3/src/basic/parse-util.c    parse_mtu, parse_size
 */

/**
 * Validator for `MTUBytes=` in `[Link]` (.network and .link) and `[NetDev]`, plus
 * `[Network] IPv6MTUBytes=` and `[DHCPv4] RouteMTUBytes=`.
 *
 * config_parse_mtu hands the value to parse_mtu, which runs parse_size() with base 1024 and then
 * range-checks the number of bytes it denotes:
 *
 * ```c
 * r = parse_size(s, 1024, &u);
 * …
 * if (u > UINT32_MAX) return -ERANGE;
 * switch (family) {
 * case AF_INET:  m = IPV4_MIN_MTU; break;    // 68
 * case AF_INET6: m = IPV6_MIN_MTU; break;    // 1280
 * default:       m = 0;
 * }
 * if (u < m) return -ERANGE;
 * ```
 *
 * The bound is therefore on the *value*, not on how it is written: `IPv6MTUBytes=1K` is 1024 bytes
 * and is rejected for being under IPV6_MIN_MTU even though nothing about its spelling looks wrong.
 * [ByteSizeTerminal] evaluates the suffix so the minimum applies to every form.
 *
 * @param minimum the family's IPV*_MIN_MTU, in bytes
 */
open class ConfigParseMtuOptionValue(minimum: Long) : SimpleGrammarOptionValues(
    "config_parse_mtu",
    SequenceCombinator(ByteSizeTerminal(minimum, UINT32_MAX), EOF())
)

private const val UINT32_MAX = 4_294_967_295L

/** `MTUBytes=` with no family-specific minimum (ltype AF_UNSPEC). */
class ConfigParseMtuAnyOptionValue : ConfigParseMtuOptionValue(0)

/** `[DHCPv4] RouteMTUBytes=` (ltype AF_INET): at least IPV4_MIN_MTU. */
class ConfigParseMtuIpv4OptionValue : ConfigParseMtuOptionValue(68)

/** `[Network] IPv6MTUBytes=` (ltype AF_INET6): at least IPV6_MIN_MTU. */
class ConfigParseMtuIpv6OptionValue : ConfigParseMtuOptionValue(1280)
