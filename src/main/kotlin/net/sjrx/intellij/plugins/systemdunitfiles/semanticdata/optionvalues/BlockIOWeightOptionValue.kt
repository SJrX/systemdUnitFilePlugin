package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*



class BlockIOWeightOptionValue() : GrammarOptionValue("config_parse_blockio_weight", GRAMMAR) {

  companion object {
    val GRAMMAR =
      SequenceCombinator(
        IntegerTerminal(10, 1001),
        EOF()
      )

    val validators = mapOf(
      Validator("config_parse_blockio_weight", "0") to BlockIOWeightOptionValue()
    )
  }
}

