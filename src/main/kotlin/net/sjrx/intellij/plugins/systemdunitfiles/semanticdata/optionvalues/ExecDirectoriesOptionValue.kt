package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

class ExecDirectoriesOptionValue : OptionValueInformation {

  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  override fun getErrorMessage(value: String): String? {
    val values = value.split("\\s+")

    // loop over element in values
    for (element in values) {
      if (element.startsWith("/")) {
        return "Invalid path: '${element}'. Takes a list of relative paths."
      } else if (element.contains("..")) {
        return "Invalid path: '${element}'. Path cannot contain `..`."
      }
      continue
    }

    return null
  }


  override val validatorName: String
    get() = VALIDATOR_NAME

  companion object {
    private val VALIDATOR_NAME = "config_parse_exec_directories"

    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to ExecDirectoriesOptionValue())
  }
}
