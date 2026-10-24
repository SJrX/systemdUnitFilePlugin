package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV4_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV6_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [Network] DNS= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#DNS=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dns.c#L211-L268   config_parse_dns
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/socket-netlink.c#L220-L354   in_addr_port_ifindex_name_from_string_auto
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L718-L734   parse_ip_port
 */

// parse_ip_port(): safe_atou16_full(…, SAFE_ATO_REFUSE_LEADING_WHITESPACE) and then 0 is refused. The
// list is split on whitespace first, so no whitespace reaches it.
private val DNS_PORT = SafeAtoTerminal(
  signed = false,
  base = 0,
  flags = setOf(SafeAtoTerminal.Flag.REFUSE_LEADING_WHITESPACE),
  minInclusive = 1L,
  maxInclusive = 65_535L,
  acceptsWhitespace = false,
  acceptsMinus = true,
)
private val DNS_IFACE = RegexTerminal("[A-Za-z0-9_][A-Za-z0-9_.-]*", "[A-Za-z0-9_][A-Za-z0-9_.-]*")
private val DNS_IFACE_SUFFIX = SequenceCombinator(LiteralChoiceTerminal("%"), DNS_IFACE)

// Everything after the first '#' is the server name; it must not be empty and is not checked further
// (sd-check accepts `1.2.3.4#!!` and `1.2.3.4#a#b`, and refuses `1.2.3.4#`).
private val DNS_SERVER_NAME_SUFFIX = SequenceCombinator(LiteralChoiceTerminal("#"), RegexTerminal("\\S+", "\\S+"))

private val DNS_IPV4_ENTRY = SequenceCombinator(
  IPV4_ADDR,
  ZeroOrOne(SequenceCombinator(LiteralChoiceTerminal(":"), DNS_PORT)),
  ZeroOrOne(DNS_IFACE_SUFFIX),
  ZeroOrOne(DNS_SERVER_NAME_SUFFIX),
)
// A bracketed address needs the port: `[::1]` alone is -EINVAL.
private val DNS_IPV6_BRACKETED_ENTRY = SequenceCombinator(
  LiteralChoiceTerminal("["),
  IPV6_ADDR,
  LiteralChoiceTerminal("]"),
  LiteralChoiceTerminal(":"),
  DNS_PORT,
  ZeroOrOne(DNS_IFACE_SUFFIX),
  ZeroOrOne(DNS_SERVER_NAME_SUFFIX),
)
private val DNS_IPV6_BARE_ENTRY = SequenceCombinator(
  IPV6_ADDR,
  ZeroOrOne(DNS_IFACE_SUFFIX),
  ZeroOrOne(DNS_SERVER_NAME_SUFFIX),
)
private val DNS_ENTRY = AlternativeCombinator(
  DNS_IPV6_BRACKETED_ENTRY,
  DNS_IPV4_ENTRY,
  DNS_IPV6_BARE_ENTRY
)

/**
 * Validator for [Network] DNS= (.network).
 *
 * C Function: config_parse_dns(0). A whitespace-separated list; each word goes through
 * in_addr_full_new_from_string → in_addr_port_ifindex_name_from_string_auto, which accepts
 * `ADDRESS[:PORT][%IFACE][#SERVERNAME]`: an IPv4 address with an optional port, a bare IPv6 address
 * (no port), or `[IPv6]:PORT`. The `#SERVERNAME` suffix was missing before, so
 * `10.10.10.10#aaa.com 10.10.10.11:1111#bbb.com [1111:2222::3333]:1234#ccc.com` was flagged although
 * sd-check accepts it. PORT is parse_ip_port (1…65535, base 0, so `:0x35` works and `:0` does not).
 *
 * Left lenient: `%IFACE` must name an existing interface (rtnl_resolve_interface), which depends on the
 * machine, so only its shape is checked.
 */
class ConfigParseDnsOptionValue : SimpleGrammarOptionValues(
  "config_parse_dns",
  SequenceCombinator(
    DNS_ENTRY,
    ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), DNS_ENTRY)),
    EOF()
  )
)
