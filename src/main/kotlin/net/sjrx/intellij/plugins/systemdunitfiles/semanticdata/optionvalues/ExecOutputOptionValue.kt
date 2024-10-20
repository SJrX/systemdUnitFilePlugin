package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

class ExecOutputOptionValue() : OptionValueInformation {
  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  override fun getErrorMessage(value: String): String? {

    if (value.startsWith("file:") || value.startsWith("append:") || value.startsWith("truncate:") || value.startsWith("fd:")) {
      val firstArg = value.split(":", limit = 2)[0]
      val secondArg = value.split(":", limit = 2)[1]

      if (secondArg.trim().isEmpty()) {
        when(firstArg) {
          "file", "append", "truncate" -> return "path must be specified after the colon."
          "fd" -> return "name must be specified after the colon."
        }
      }

      // We could do a better job of validating paths later
      return null
    }

    when (value) {
      "inherit", "null", "tty", "journal", "kmsg", "journal+console", "kmsg+console", "socket" -> return null
      else -> return "Takes one of inherit, null, tty, journal, kmsg, journal+console, kmsg+console, file:path, append:path, truncate:path, socket or fd:name."

    }
  }

  override val validatorName: String
    get() = VALIDATOR_NAME


  companion object {
    private val VALIDATOR_NAME = "config_parse_exec_output"

    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to ExecOutputOptionValue())
  }
}
