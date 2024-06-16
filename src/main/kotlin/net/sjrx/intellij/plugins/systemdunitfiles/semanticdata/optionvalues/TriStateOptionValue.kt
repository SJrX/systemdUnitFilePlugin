package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.google.common.collect.ImmutableSet
import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import java.util.*

/**
 * This validator is for the config_parse_bool validator.
 *
 *
 * The code for this is in: parse-util.c ~ line 21
 *
 */
class TriStateOptionValue : OptionValueInformation {
  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return autoCompleteValues
  }

  override fun getErrorMessage(value: String): String? {
    return if (validValuesLowerCase.contains(value.lowercase(Locale.getDefault()))) {
      null
    } else {
      "This option takes a boolean value, " + value + " must be one of: " + validValuesLowerCase +  " or the empty string"
    }
  }

  override val validatorName: String
    get() = VALIDATOR_NAME

  companion object {
    private val validValuesLowerCase: Set<String> = ImmutableSet.of("", "1", "yes", "y", "true", "t", "on", "0", "no", "n", "false", "f", "off")
    private val autoCompleteValues: Set<String> = ImmutableSet.of("on", "off", "yes", "no", "true", "false")
    const val VALIDATOR_NAME = "config_parse_tristate"

    val validators = mapOf(
      Validator(VALIDATOR_NAME, "0") to TriStateOptionValue(),

    )


  }
}
