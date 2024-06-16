package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

class TtySizeOptionValue : OptionValueInformation {

  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  override fun getErrorMessage(value: String): String? {
    try {
      Integer.parseUnsignedInt(value)
      return null
    } catch(e : NumberFormatException) {
      return "This value must be an unsigned integer"
    }
  }

  override val validatorName: String
    get() = VALIDATOR_NAME

  companion object {
    const val VALIDATOR_NAME="config_parse_tty_size"

    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to TtySizeOptionValue())
  }
}
