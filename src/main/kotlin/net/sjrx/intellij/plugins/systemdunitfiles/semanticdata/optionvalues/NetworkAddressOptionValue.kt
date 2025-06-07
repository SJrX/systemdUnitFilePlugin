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

    val validators = mapOf(
      Validator("config_parse_address_section", "ADDRESS_ADDRESS") to NetworkAddressOptionValue()
    )
  }
}

