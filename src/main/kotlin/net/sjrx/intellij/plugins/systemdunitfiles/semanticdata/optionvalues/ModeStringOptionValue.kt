package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project

/**
 * Matches a mode value written in octal.
 *
 *
 * Looks like this was done internally in C, but test-conf-parser.c ~ line 172
 * has some stuff.
 */
class ModeStringOptionValue : OptionValueInformation {
  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  override fun getErrorMessage(value: String): String? {
    return if (value.matches("[0-7]{3,4}".toRegex())) {
      null
    } else {
      "Value is expected to be a 3 or 4 digit octal number not: $value"
    }
  }

  override val validatorName: String
    get() = "config_parse_mode"
}
