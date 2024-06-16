package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ConfigParseSecOptionValue.Companion.VALIDATOR_NAME

abstract class AbstractConfigParseSecOptionValue(override val validatorName: String) : OptionValueInformation {

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
}

class ConfigParseSecOptionValue : AbstractConfigParseSecOptionValue(VALIDATOR_NAME){
  companion object {
    const val VALIDATOR_NAME = "config_parse_sec"
  }
}

class ConfigParseSecFix0OptionValue : AbstractConfigParseSecOptionValue(VALIDATOR_NAME){
  companion object {
    const val VALIDATOR_NAME = "config_parse_sec_fix_0"
  }
}

class ConfigParseSecDefInifinityOptionValue : AbstractConfigParseSecOptionValue(VALIDATOR_NAME){
  companion object {
    const val VALIDATOR_NAME = "config_parse_sec_def_infinity"
  }
}


object ConfigParseSecValidators {
  val validators = mapOf(
    Validator(ConfigParseSecOptionValue.VALIDATOR_NAME, "0") to ConfigParseSecOptionValue(),
    Validator(ConfigParseSecFix0OptionValue.VALIDATOR_NAME, "0") to ConfigParseSecFix0OptionValue(),
    Validator(ConfigParseSecDefInifinityOptionValue.VALIDATOR_NAME, "0") to ConfigParseSecDefInifinityOptionValue()
  )
}
