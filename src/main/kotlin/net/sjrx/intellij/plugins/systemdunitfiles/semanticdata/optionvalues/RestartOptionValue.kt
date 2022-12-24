package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.google.common.collect.ImmutableSet
import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

/**
 * This validator is for the config_service_restart validator.
 *
 *
 * This seems to use the values referenced in:
 *
 *
 * service.c ~ line 3978
 *
 */
class RestartOptionValue : AbstractEnumOptionValue(validOptions, VALIDATOR_NAME) {

  companion object {
    private val validOptions: Set<String> = ImmutableSet.of("no", "on-success", "on-failure", "on-abnormal", "on-watchdog", "on-abort", "always")
    const val VALIDATOR_NAME = "config_parse_service_restart"

    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to RestartOptionValue())

  }
}
