package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV6_PREFIX_SAFE_LEN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [IPv6RoutePrefix] Route= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Route=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-radv.c#L1076-L1114    config_parse_route_prefix (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L799-L843       in_addr_prefix_from_string (AF_INET6)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L779-L797       in_addr_parse_prefixlen (safe_atou8, ≤ 128)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-ipv6ra-prefix.network#L24-L26
 * linux  https://man7.org/linux/man-pages/man3/inet_pton.3.html
 */

/**
 * Validator for `[IPv6RoutePrefix] Route=` (.network).
 *
 * config_parse_route_prefix reads an IPv6 prefix with in_addr_prefix_from_string(AF_INET6): an IPv6
 * literal (inet_pton — no IPv4, no zone) and an optional `/LEN` in 0…128, LEN read with safe_atou8 so
 * hex/octal spellings count too. The man page asks for the slash, but without it the C simply uses a
 * /128, so that is accepted. Host bits are masked off silently, so `2001:db8::1/64` is fine.
 */
class ConfigParseRoutePrefixOptionValue : SimpleGrammarOptionValues(
    "config_parse_route_prefix",
    SequenceCombinator(IPV6_PREFIX_SAFE_LEN, EOF())
)
