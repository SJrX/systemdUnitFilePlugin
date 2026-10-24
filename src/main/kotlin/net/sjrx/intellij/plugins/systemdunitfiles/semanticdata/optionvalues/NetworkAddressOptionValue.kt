package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CIDR_SEPARATOR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV4_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV6_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [Address] Address= (and the [Network] Address= shorthand) in a .network file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Address=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-address.c#L2131-L2172   config_parse_address
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L2114-L2149   config_parse_in_addr_prefix
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L779-L797   in_addr_parse_prefixlen (safe_atou8)
 */

/**
 * Validator for `Address=`, the ADDRESS_ADDRESS slot of config_parse_address_section.
 *
 * config_parse_address delegates to config_parse_in_addr_prefix asking for PREFIXLEN_REFUSE. When the
 * prefix length is missing that returns -ENOANO, and the caller retries with
 * in_addr_prefix_from_string_auto and only logs:
 *
 * > Address=… is specified without prefix length. Assuming the prefix length is N.
 * > Please specify the prefix length explicitly.
 *
 * That is advice, not a rejection: the address is used with the full-width prefix. So a bare address is
 * accepted for both families (`Address=2001:db8::1` used to be flagged for IPv6; issue #568).
 *
 * The prefix length goes through in_addr_parse_prefixlen → safe_atou8 (base 0, so `/0x40` works, and
 * whitespace after the slash reaches it: `/ 8`), and is refused only when wider than the address, so the
 * full 0…32 and 0…128 ranges are legal (systemd's own test/test-network/conf/25-veth-peer.network uses
 * `Address=2600::1/0`). All confirmed with sd-check at 0f0682b773.
 *
 * `Peer=` is the same ConfigSectionParser slot; see ConfigParseAddressSectionPeerOptionValue.
 */
class NetworkAddressOptionValue() : GrammarOptionValue("config_parse_address_section", GRAMMAR) {

  companion object {
    val GRAMMAR = SequenceCombinator(
      AlternativeCombinator(
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
        ),
        SequenceCombinator(
          IPV4_ADDR,
          ZeroOrOne(
            SequenceCombinator(
              CIDR_SEPARATOR,
              SafeAtoTerminal(
                signed = false,
                base = 0,
                flags = emptySet(),
                minInclusive = 0L,
                maxInclusive = 32L,
                acceptsWhitespace = true,
                acceptsMinus = true,
              ),
            ),
          ),
        ),
      ),
      EOF()
    )

    val validators = mapOf(
      Validator("config_parse_address_section", "ADDRESS_ADDRESS") to NetworkAddressOptionValue()
    )
  }
}
