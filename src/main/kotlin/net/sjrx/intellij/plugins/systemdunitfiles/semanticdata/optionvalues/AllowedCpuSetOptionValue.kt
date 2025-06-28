package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import java.util.regex.Pattern

class AllowedCpuSetOptionValue(val name : String) : OptionValueInformation  {
  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  override fun getErrorMessage(value: String): String? {
    val cpuSetElement = value.split(" ", ",")

    // Loop over cpuSetElement and for each element, if it's an integer do continue
    // if it's a range value that the first value is less than the second

    for (element in cpuSetElement) {

      if (element.length == 0) {
        continue
      }

      // Note we don't validate the input very much because
      // the regex is very tight, so we know we have integers.

      if (element.matches(CPU_ELEMENT_REGEX.toRegex())) {
        val range = element.split("-")
        if (range.size == 2) {


          val start = range[0].toInt()
          val end = range[1].toInt()
          if (start > end) {
            return "The start %d of the range must be less than the end %d of the range".format(start, end)
          }
        }

      } else {
        return "Invalid syntax for CPU Range: %s".format(element)
      }
    }

    return null


  }

  override val validatorName: String
    get() = name

  companion object {
    // Old validator name (before June 28th 2025)
    const val OLD_VALIDATOR_NAME = "config_parse_allowed_cpuset"

    // New validator name (after June 28th 2025)
    const val NEW_VALIDATOR_NAME = "config_parse_unit_cpu_set"

    private val CPU_ELEMENT_REGEX= Pattern.compile("^[0-9]+(-[0-9]+)?$")
    val validators = mapOf(
      Validator(OLD_VALIDATOR_NAME) to AllowedCpuSetOptionValue(OLD_VALIDATOR_NAME),
      Validator(NEW_VALIDATOR_NAME) to AllowedCpuSetOptionValue(NEW_VALIDATOR_NAME),
      )

  }
}
