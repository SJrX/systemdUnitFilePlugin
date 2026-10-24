package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * [DHCPv6] PrefixDelegationHint= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#PrefixDelegationHint=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp6.c#L1010-L1048   config_parse_dhcp6_pd_prefix_hint
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L799-L843   in_addr_prefix_from_string (no slash: length 128)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L779-L797   in_addr_parse_prefixlen (safe_atou8, at most 128)
 */

/**
 * Validator for [DHCPv6] PrefixDelegationHint= (.network).
 *
 * C Function: config_parse_dhcp6_pd_prefix_hint(0). in_addr_prefix_from_string(…, AF_INET6, …) takes an
 * IPv6 address with an optional `/LEN`; without one the length is 128, so `2001:db8::` is accepted
 * (sd-check at 0f0682b773). LEN is read with safe_atou8 (base 0, `/0b1` works) and the parser then
 * requires 1…128.
 */
class ConfigParseDhcp6PdPrefixHintOptionValue : SimpleGrammarOptionValues(
  "config_parse_dhcp6_pd_prefix_hint",
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
