package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues;

import com.google.common.collect.ImmutableSet;

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
  
  private static final Set<String> autoCompleteValues = ImmutableSet.of("on", "off");

  @Override
  public Set<String> getAutoCompleteOptions() {
    return autoCompleteValues;
  }
  
  @Override
  public boolean isValidValue(String value) {
    return validValuesLowerCase.contains(value.toLowerCase());
  }
  
  @Override
  public String getValidatorName() {
    return "config_parse_bool";
  }
}
