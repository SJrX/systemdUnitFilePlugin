package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType

interface OptionValueInformation {
  /**
   * Returns the set of auto complete options for this option.
   */
  fun getAutoCompleteOptions(project: Project): Set<String>

  /**
   * Validates whether the value passed is valid.
   *
   * @param value - the value
   * @return `null` if the value is fine, some string describing the error if there is a problem.
   */
  fun getErrorMessage(value: String): String?

  /**
   * Generates problem descriptors based on the value.
   *
   * @param property - the Psi Element we are examining.
   * @param holder - A problem holder that we should add to.
   */
  fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
    val value = property.valueText ?: return
    val errorMessage = getErrorMessage(value)
    if (errorMessage != null) {
      holder.registerProblem(property.valueNode.psi, errorMessage, ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
    }
  }

  /**
   * Get the name of the validator that this implements.
   *
   * @return validator name
   */
  val validatorName: String
}
