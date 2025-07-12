package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*



class UIntOptionValues(validatorName: String, grammar: Combinator) : GrammarOptionValue(validatorName, grammar) {

  companion object {

    val validators = mapOf(
      Validator("config_parse_uint32", "0") to UIntOptionValues("config_parse_uint32", SequenceCombinator(IntegerTerminal(0, 4_294_967_296 ), EOF())),
      Validator("config_parse_uint16", "0") to UIntOptionValues("config_parse_uint16", SequenceCombinator(IntegerTerminal(0, 65536), EOF())),
      Validator("config_parse_uint8", "0") to UIntOptionValues("config_parse_uint8", SequenceCombinator(IntegerTerminal(0, 256), EOF())),
    )
  }
}

