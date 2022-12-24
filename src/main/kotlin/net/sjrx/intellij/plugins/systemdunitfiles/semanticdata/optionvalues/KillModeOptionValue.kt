package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.google.common.collect.ImmutableSet
import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

/**
 * This validator is for the config_kill_mode validator.
 *
 *
 * This seems to use the values referenced in:
 *
 *
 * kill.c ~ line 35.
 */
class KillModeOptionValue : AbstractEnumOptionValue(validOptions, VALIDATOR_NAME) {

  companion object {
    private val validOptions: Set<String> = ImmutableSet.of("control-group", "process", "mixed", "none")
    const val VALIDATOR_NAME = "config_parse_kill_mode"
    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to KillModeOptionValue())
  }
}


