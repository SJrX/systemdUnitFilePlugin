package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.google.common.collect.ImmutableSet
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

class ManagedOOMModeOptionValue : AbstractEnumOptionValue(validOptions, VALIDATOR_NAME){

  companion object {
    private val validOptions : Set<String> = ImmutableSet.of("auto", "kill")
    const val VALIDATOR_NAME = "config_parse_managed_oom_mode"
    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to ManagedOOMModeOptionValue())
  }
}
