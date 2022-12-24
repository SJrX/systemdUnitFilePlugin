package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

/**
 * This validator has no auto completion and marks everything as valid.
 */
class NullOptionValue : OptionValueInformation {
  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  override fun getErrorMessage(value: String): String? {
    return null
  }

  override val validatorName: String
    get() = "NULL"

  companion object {
    const val VALIDATOR_NAME = "NULL"
    val INSTANCE = NullOptionValue()
    val VALIDATOR_INSTANCE = Validator(VALIDATOR_NAME, "0")
    val validators = mapOf(VALIDATOR_INSTANCE to INSTANCE, Validator("config_parse_warn_compat", "*") to INSTANCE)
  }
}
