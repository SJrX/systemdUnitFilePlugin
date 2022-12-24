package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

class MemoryLimitOptionValue() : OptionValueInformation {


  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  override fun getErrorMessage(value: String): String? {

    if (BYTES.matches(value)) {
      return null
    }

    if (INFINITY.matches(value)) {
      return null
    }

    if (VALID_PERCENTAGE.matches(value)) {
      return null
    }

    return "Invalid memory limit: '${value}'. Takes a memory size in bytes. Can be suffixed with K, M, G, T to specify Kilo/Mega/Giga/Terabytes respectively. Alternatively a percentage (with up to 2 decimal points) may be specified, or the special value 'infinity'."
  }

  override val validatorName: String
    get() = VALIDATOR_NAME



  companion object {
    const val VALIDATOR_NAME = "config_parse_memory_limit"
    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to MemoryLimitOptionValue())

    val BYTES = """^\s*[0-9]+(\.[0-9]*)?\s*[KMGT]?\s*$""".toRegex()
    val VALID_PERCENTAGE = """^\s*(100|([0-9]?[0-9]))(\.[0-9]{1,2})?\s*%\s*$""".toRegex()
    val INFINITY = """^\s*infinity\s*$""".toRegex()


  }
}
