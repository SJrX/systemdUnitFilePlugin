package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project

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
}
