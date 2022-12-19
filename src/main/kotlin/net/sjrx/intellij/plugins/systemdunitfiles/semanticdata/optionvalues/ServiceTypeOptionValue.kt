package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.google.common.collect.ImmutableSet
import com.intellij.openapi.project.Project

/**
 * This validator is for the config_service_type validator.
 *
 *
 * This seems to use the values referenced in:
 *
 *
 * service_type_table ~ line 3990.
 */
class ServiceTypeOptionValue : OptionValueInformation {
  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return validOptions
  }

  override fun getErrorMessage(value: String): String? {
    return if (validOptions.contains(value)) {
      null
    } else {
      "Expected value " + value + " does not match one of the expected options: " + validOptions
    }
  }

  override val validatorName: String
    get() = "config_parse_service_type"

  companion object {
    private val validOptions: Set<String> = ImmutableSet.of("simple", "forking", "oneshot", "dbus", "notify", "idle", "exec")
  }
}
