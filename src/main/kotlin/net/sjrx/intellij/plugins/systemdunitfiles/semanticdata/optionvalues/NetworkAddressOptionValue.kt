package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CIDR_SEPARATOR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV4_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV6_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IntegerTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [Address] Address= (and the [Network] Address= shorthand) in a .network file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Address=
 * parser https://github.com/systemd/systemd/blob/a8e93919c3/src/network/networkd-address.c  config_parse_address
 *        https://github.com/systemd/systemd/blob/a8e93919c3/src/shared/conf-parser.c        config_parse_in_addr_prefix
 */

/**
 * Validator for `Address=`, the ADDRESS_ADDRESS slot of config_parse_address_section.
 *
 * config_parse_address delegates to config_parse_in_addr_prefix asking for PREFIXLEN_REFUSE. When the
 * prefix length is missing that returns -ENOANO, and the caller retries with
 * in_addr_prefix_from_string_auto and logs:
 *
 * > Address=… is specified without prefix length. Assuming the prefix length is N.
 * > Please specify the prefix length explicitly.
 *
 * Requiring the prefix on IPv6 therefore matches systemd's own advice rather than over-reaching, and
 * that behaviour is kept. What is *not* faithful is bounding the prefix: in_addr_prefix_from_string
 * only rejects a length above the family's address width, so the full 0…32 and 0…128 ranges are
 * legal. The previous grammar demanded /8…/32 and /64…/128, which flagged `Address=2600::1/0` — a
 * line out of systemd's own test/test-network/conf/25-veth-peer.network — and `Address=…/7`.
 *
 * `Peer=` is the same ConfigSectionParser slot but takes the fully faithful grammar, since it has no
 * comparable history; see ConfigParseAddressSectionPeerOptionValue.
 */
class NetworkAddressOptionValue() : GrammarOptionValue("config_parse_address_section", GRAMMAR) {

  companion object {
    val GRAMMAR = SequenceCombinator(
      AlternativeCombinator(
        SequenceCombinator(IPV6_ADDR, CIDR_SEPARATOR, IntegerTerminal(0, 129)),
        SequenceCombinator(IPV4_ADDR, ZeroOrOne(SequenceCombinator(CIDR_SEPARATOR, IntegerTerminal(0, 33)))),
      ),
      EOF()
    )

    val validators = mapOf(
      Validator("config_parse_address_section", "ADDRESS_ADDRESS") to NetworkAddressOptionValue()
    )
  }
}
