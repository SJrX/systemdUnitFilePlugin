package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * This validator is for the config_service_restart validator.
 * <p></p>
 * This seems to use the values referenced in:
 *<p></p>
 * service.c ~ line 3978
 *
 */
public class RestartOptionValue implements OptionValueInformation {
  
  private static final Set<String> validOptions
    = ImmutableSet.of("no", "on-success", "on-failure", "on-abnormal", "on-watchdog", "on-abort", "always");
  
  
  @Override
  public Set<String> getAutoCompleteOptions() {
    return validOptions;
  }
  
  @Override
  public String getErrorMessage(String value) {
  
    if (validOptions.contains(value)) {
      return null;
    } else {
      return "Expected value " + value + " does not match one of the expected options: " + validOptions;
    }
  }
  
  @Override
  public String getValidatorName() {
    return "config_parse_service_restart";
  }
}
