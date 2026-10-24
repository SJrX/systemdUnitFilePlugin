package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * [IPv6Prefix] Prefix= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Prefix=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-radv.c#L872-L910   config_parse_prefix
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L799-L843   in_addr_prefix_from_string (no slash: length 128)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L779-L797   in_addr_parse_prefixlen (safe_atou8, at most 128)
 */

/**
 * Validator for [IPv6Prefix] Prefix= (.network).
 *
 * C Function: config_parse_prefix(0). in_addr_prefix_from_string(…, AF_INET6, …) takes an IPv6 address
 * with an optional `/LEN` (safe_atou8, base 0, at most 128); without one the length is 128, so
 * `2001:db8::` is accepted (sd-check at 0f0682b773). The parser masks the address to the length and
 * refuses nothing else, so `/0` is accepted too.
 */
class ConfigParsePrefixOptionValue : SimpleGrammarOptionValues(
  "config_parse_prefix",
  SequenceCombinator(
    IPV6_ADDR,
    ZeroOrOne(
      SequenceCombinator(
        CIDR_SEPARATOR,
        SafeAtoTerminal(
          signed = false,
          base = 0,
          flags = emptySet(),
          minInclusive = 0L,
          maxInclusive = 128L,
          acceptsWhitespace = true,
          acceptsMinus = true,
        ),
      ),
    ),
    EOF(),
  ),
)
