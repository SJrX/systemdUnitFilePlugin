package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

class PathOptionValue : OptionValueInformation {
  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  override fun getErrorMessage(value: String): String? {
    return if (PATH_REGEX.matches(value)) {
      null
    } else {
      "This option must be a valid absolute path, not [$value]"
    }
  }

  override val validatorName: String
    get() = VALIDATOR_NAME

  companion object {
    const val VALIDATOR_NAME = "config_parse_unit_path_printf"

    val validators = mapOf(
      Validator(VALIDATOR_NAME, "0") to PathOptionValue(),
      Validator(VALIDATOR_NAME, "true") to PathOptionValue(),
                           )

    val PATH_REGEX = """^/(?:[^\\ ]|\\\s|(?:\\\\))+$""".toRegex()

  }
}
