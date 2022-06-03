package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This validator is used for the config_parse_exec validator.
 * <p/>
 * This validator does nothing because other inspections use it to determine if they should run,
 * kind of hacky I know.
 */
public class ExecOptionValue implements OptionValueInformation {
  
  private static final Pattern RELATIVE_PATH_REGEX = Pattern.compile("^\\s*([^\\/\\s]\\S*)\\s*");
  
  
  @Override
  public Set<String> getAutoCompleteOptions(@NotNull Project project) {
    return Collections.emptySet();
  }
  
  /**
   * Returns problem descriptors.
   * @param property - the Psi Element we are examining.
   * @param holder - A problem holder that we should add to.
   */
  public void generateProblemDescriptors(@NotNull UnitFilePropertyType property, @NotNull ProblemsHolder holder) {
  
    String values = property.getValueNode().getText();
    
    Matcher m = RELATIVE_PATH_REGEX.matcher(values);
    
    if (m.find()) {
      MatchResult mr = m.toMatchResult();
      
      TextRange range = new TextRange(mr.start(1), mr.end(1));
      
      holder.registerProblem(
        property.getValueNode().getPsi(),
        "It is recommended to use an absolute path to avoid ambiguity. If the command is not a full (absolute) path, it"
        + " will be resolved to a full path using a fixed search path (that depends on the distribution) determined at compilation time.",
        ProblemHighlightType.WEAK_WARNING, range);
    }
    
  }
  
  @Override
  public String getErrorMessage(String values) {
    return null;
  }
  
  @Override
  public String getValidatorName() {
    return "config_parse_exec";
  }
}
