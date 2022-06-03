package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface OptionValueInformation {
  
  /**
   * Returns the set of auto complete options for this option.
   */
  public Set<String> getAutoCompleteOptions(@NotNull Project project);
  
  /**
   * Validates whether the value passed is valid.
   *
   * @param value - the value
   * @return <code>null</code> if the value is fine, some string describing the error if there is a problem.
   */
  public String getErrorMessage(String value);
  
  /**
   * Generates problem descriptors based on the value.
   *
   * @param property - the Psi Element we are examining.
   * @param holder - A problem holder that we should add to.
   */
  default void generateProblemDescriptors(@NotNull UnitFilePropertyType property, @NotNull ProblemsHolder holder) {
    String value = property.getValueText();
    if (value == null) {
      return;
    }
  
    String errorMessage = this.getErrorMessage(value);
  
    if (errorMessage != null) {
      holder.registerProblem(property.getValueNode().getPsi(), errorMessage, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    }
  }
  
  /**
   * Get the name of the validator that this implements.
   *
   * @return validator name
   */
  public String getValidatorName();
  
}
