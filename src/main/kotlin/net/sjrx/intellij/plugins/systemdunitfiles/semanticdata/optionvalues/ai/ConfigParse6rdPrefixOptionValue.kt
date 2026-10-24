package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * [Tunnel] IPv6RapidDeploymentPrefix= (.netdev)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#IPv6RapidDeploymentPrefix=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/tunnel.c#L979-L1014   config_parse_6rd_prefix
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L799-L843   in_addr_prefix_from_string (no slash: length 128)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L779-L797   in_addr_parse_prefixlen (safe_atou8, at most 128)
 */

/**
 * Validator for [Tunnel] IPv6RapidDeploymentPrefix= (.netdev).
 *
 * C Function: config_parse_6rd_prefix(0). in_addr_prefix_from_string(…, AF_INET6, …) takes an IPv6
 * address with an optional `/LEN`; without one the length is 128, so `2001:db8::` and `2001:db8::1` are
 * accepted (sd-check at 0f0682b773). LEN is read with safe_atou8 (base 0, `/0x10` works) and may be at
 * most 128; config_parse_6rd_prefix then refuses a length of 0.
 */
class ConfigParse6rdPrefixOptionValue : SimpleGrammarOptionValues(
  "config_parse_6rd_prefix",
  SequenceCombinator(
    IPV6_ADDR,
    ZeroOrOne(
      SequenceCombinator(
        CIDR_SEPARATOR,
        SafeAtoTerminal(
          signed = false,
          base = 0,
          flags = emptySet(),
          minInclusive = 1L,
          maxInclusive = 128L,
          acceptsWhitespace = true,
          acceptsMinus = true,
        ),
      ),
    ),
    EOF(),
  ),
)
