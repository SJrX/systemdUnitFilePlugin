package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project

abstract class AbstractEnumOptionValue(private val validOptions: Set<String>, override val validatorName: String): OptionValueInformation {

  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return validOptions
  }


  override fun getErrorMessage(value: String): String? {
    return if (!validOptions.contains(value)) {
      "The value supplied $value does not match one of the expected values: $validOptions"
    } else {
      null
    }
  }

}
