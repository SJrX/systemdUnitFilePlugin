package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IP_ADDR_AND_PREFIX_LENGTH
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

class NetworkAddressOptionValue() : GrammarOptionValue("config_parse_address_section", GRAMMAR) {

  companion object {
    val GRAMMAR = SequenceCombinator(
      IP_ADDR_AND_PREFIX_LENGTH, EOF())

    // Address= and Peer= are the same ConfigSectionParser entry (config_parse_address), so they get
    // the same grammar. Note it is deliberately stricter than systemd's in_addr_prefix_from_string_auto,
    // which would also allow an IPv4 prefix below /8 and an IPv6 address with no prefix at all.
    val validators = mapOf(
      Validator("config_parse_address_section", "ADDRESS_ADDRESS") to NetworkAddressOptionValue(),
      Validator("config_parse_address_section", "ADDRESS_PEER") to NetworkAddressOptionValue()
    )
  }
}

