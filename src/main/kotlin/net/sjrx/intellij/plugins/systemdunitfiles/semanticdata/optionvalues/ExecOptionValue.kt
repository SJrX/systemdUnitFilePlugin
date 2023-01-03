package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import java.util.regex.Pattern

/**
 * This validator is used for the config_parse_exec validator.
 *
 *
 * This validator does nothing because other inspections use it to determine if they should run,
 * kind of hacky I know.
 */
class ExecOptionValue : OptionValueInformation {
  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  /**
   * Returns problem descriptors.
   * @param property - the Psi Element we are examining.
   * @param holder - A problem holder that we should add to.
   */
  override fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
    val values = property.valueNode.text
    val absMatcher = ABSOLUTE_PATH_REGEX.matcher(values)
    if (absMatcher.find()) {
      return
    }
    val m = RELATIVE_PATH_REGEX.matcher(values)
    if (m.find()) {
      val mr = m.toMatchResult()
      val range = TextRange(mr.start(1), mr.end(1))
      holder.registerProblem(
        property.valueNode.psi, "It is recommended to use an absolute path to avoid ambiguity. If the command is not a full (absolute) path, it"
          + " will be resolved to a full path using a fixed search path (that depends on the distribution) determined at compilation time.",
        ProblemHighlightType.WEAK_WARNING, range
      )
    }
  }

  override fun getErrorMessage(value: String): String? {
    return null
  }

  override val validatorName: String
    get() = VALIDATOR_NAME

  companion object {
    // Used to check whether or not there is a problem.
    private val ABSOLUTE_PATH_REGEX = Pattern.compile("^\\s*(?:[+@:!-]|!!)*\\s*[\\/]")

    // Used to determine what to highlight (we want to avoid highlighting valid prefixes).
    private val RELATIVE_PATH_REGEX = Pattern.compile("^\\s*(?:[+@:!-]+)?([^\\/\\s]\\S*)\\s*")

    const val VALIDATOR_NAME = "config_parse_exec"

    val validators = mapOf(Validator(VALIDATOR_NAME, "*") to ExecOptionValue())
  }
}
