package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues;

import com.google.common.collect.ImmutableSet;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * This validator is for the config_parse_bool validator.
 * <p></p>
 * The code for this is in: parse-util.c ~ line 21
 *
 */
public class BooleanOptionValue implements OptionValueInformation {
  
  private static final Set<String> validValuesLowerCase
    = ImmutableSet.of("1","yes","y","true","t", "on", "0","no", "n", "false", "f", "off");
  
  private static final Set<String> autoCompleteValues = ImmutableSet.of("on", "off", "yes", "no", "true", "false");

  @Override
  public Set<String> getAutoCompleteOptions(@NotNull Project project) {
    return autoCompleteValues;
  }
  
  @Override
  public String getErrorMessage(String value) {
    if (validValuesLowerCase.contains(value.toLowerCase())) {
      return null;
    } else {
      return "This option takes a boolean value, " + value + " must be one of: " + validValuesLowerCase;
    }
  }
  
  @Override
  public String getValidatorName() {
    return "config_parse_bool";
  }
}
