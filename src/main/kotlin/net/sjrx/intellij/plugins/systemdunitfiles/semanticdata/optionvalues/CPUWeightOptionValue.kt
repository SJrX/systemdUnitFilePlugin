package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*


class CPUWeightOptionValue : GrammarOptionValue(validatorName, GRAMMAR) {

  companion object {
    val validatorName = "config_parse_cg_cpu_weight"

    val GRAMMAR =
      SequenceCombinator(
        AlternativeCombinator(
          FlexibleLiteralChoiceTerminal("idle"),
          IntegerTerminal(1, 10001)
        ),
        EOF()
    )

    val validators = mapOf(
      Validator(validatorName, "0") to CPUWeightOptionValue()
    )
  }
}
