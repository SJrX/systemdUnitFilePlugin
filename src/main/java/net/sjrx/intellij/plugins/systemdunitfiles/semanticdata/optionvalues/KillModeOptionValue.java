package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * This validator is for the config_kill_mode validator.
 * <p></p>
 * This seems to use the values referenced in:
 * <p></p>
 * kill.c ~ line 35.
 */
public class KillModeOptionValue implements OptionValueInformation {
  
  private static final Set<String> validOptions = ImmutableSet.of("control-group", "process", "mixed", "none");
  
  @Override
  public Set<String> getAutoCompleteOptions() {
    return validOptions;
  }
  
  @Override
  public String getErrorMessage(String value) {
    if (!validOptions.contains(value)) {
      return "The value supplied " + value + " does not match one of the expected values: " + validOptions;
    } else {
      return null;
    }
  }
  
  @Override
  public String getValidatorName() {
    return "config_parse_kill_mode";
  }
}
