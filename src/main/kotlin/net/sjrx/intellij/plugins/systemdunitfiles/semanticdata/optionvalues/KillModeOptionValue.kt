package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.google.common.collect.ImmutableSet
import com.intellij.openapi.project.Project

/**
 * This validator is for the config_kill_mode validator.
 *
 *
 * This seems to use the values referenced in:
 *
 *
 * kill.c ~ line 35.
 */
class KillModeOptionValue : OptionValueInformation {
  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return validOptions
  }

  override fun getErrorMessage(value: String): String? {
    return if (!validOptions.contains(value)) {
      "The value supplied " + value + " does not match one of the expected values: " + validOptions
    } else {
      null
    }
  }

  override val validatorName: String
    get() = "config_parse_kill_mode"

  companion object {
    private val validOptions: Set<String> = ImmutableSet.of("control-group", "process", "mixed", "none")
  }
}
