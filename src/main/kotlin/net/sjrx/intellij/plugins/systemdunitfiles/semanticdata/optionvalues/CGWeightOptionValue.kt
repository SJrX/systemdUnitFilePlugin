package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*



class CGWeightOptionValue() : GrammarOptionValue("config_parse_cg_weight", GRAMMAR) {

  companion object {
    val GRAMMAR =
      SequenceCombinator(
        IntegerTerminal(1, 10001),
        EOF()
      )

    val validators = mapOf(
      Validator("config_parse_cg_weight", "0") to CGWeightOptionValue()
    )
  }
}

