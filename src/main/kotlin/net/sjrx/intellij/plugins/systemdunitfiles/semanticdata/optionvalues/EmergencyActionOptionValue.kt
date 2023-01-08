package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.google.common.collect.ImmutableSet
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

class EmergencyActionOptionValue : AbstractEnumOptionValue(validOptions, VALIDATOR_NAME){

  companion object {
    private val validOptions : Set<String> = ImmutableSet.of("none", "reboot", "reboot-force", "reboot-immediate", "poweroff", "poweroff-force", "poweroff-immediate", "exit", "exit-force")
    const val VALIDATOR_NAME = "config_parse_emergency_action"
    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to EmergencyActionOptionValue())
  }
}
