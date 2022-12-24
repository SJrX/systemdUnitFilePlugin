package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.google.common.collect.ImmutableSet
import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

/**
 * This validator is for the config_service_type validator.
 *
 *
 * This seems to use the values referenced in:
 *
 *
 * service_type_table ~ line 3990.
 */
class ServiceTypeOptionValue : AbstractEnumOptionValue(validOptions, VALIDATOR_NAME) {

  companion object {
    private val validOptions: Set<String> = ImmutableSet.of("simple", "forking", "oneshot", "dbus", "notify", "idle", "exec")

    const val VALIDATOR_NAME = "config_parse_service_type"
    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to ServiceTypeOptionValue())
  }
}
