package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

class InAddrPrefixesOptionValue(combinator: Combinator) : GrammarOptionValue("config_parse_in_addr_prefixes", combinator) {

  companion object {

    val validators = mapOf(
      Validator("config_parse_in_addr_prefixes", "AF_UNSPEC") to InAddrPrefixesOptionValue(SequenceCombinator(IP_ADDR_PREFIX_LIST, EOF())),
      Validator("config_parse_in_addr_prefixes", "AF_INET") to InAddrPrefixesOptionValue(SequenceCombinator(IPV4_ADDR_PREFIX_LIST, EOF())),
      Validator("config_parse_in_addr_prefixes", "AF_INET6") to InAddrPrefixesOptionValue(SequenceCombinator(IPV6_ADDR_PREFIX_LIST, EOF()))
    )
  }
}

