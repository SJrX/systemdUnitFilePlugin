package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*


class CPUSharesOptionValue : GrammarOptionValue(validatorName, GRAMMAR) {

  companion object {
    val validatorName = "config_parse_cpu_shares"

    val GRAMMAR =
      SequenceCombinator(
          IntegerTerminal(2, 262145),
          EOF()
    )

    val validators = mapOf(
      Validator(validatorName, "0") to CPUSharesOptionValue()
    )
  }
}
