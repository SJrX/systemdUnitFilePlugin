package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

class ConfigParseSecOptionValue : OptionValueInformation {
  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  override fun getErrorMessage(value: String): String? {
    try {
      TimeHelper.parseSecs(value)
      return null
    } catch (e : IllegalArgumentException) {
      return "Invalid value: ${e.message}"

    }
  }

  override val validatorName : String
    get() = VALIDATOR_NAME


  companion object {
    const val VALIDATOR_NAME = "config_parse_sec"

    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to ConfigParseSecOptionValue())
  }
}
