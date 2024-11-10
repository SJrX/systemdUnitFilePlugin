package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*



class IOLimitOptionValue() : GrammarOptionValue("config_parse_io_limit", GRAMMAR) {

  companion object {
    val GRAMMAR = SequenceCombinator(OneOrMore(SequenceCombinator(DEVICE, BYTES)), EOF())

    val validators = mapOf(
      Validator("config_parse_io_limit", "0") to IOLimitOptionValue()
    )
  }
}

