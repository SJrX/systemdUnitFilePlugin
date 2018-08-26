package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * This validator is for the config_service_type validator.
 * <p></p>
 * This seems to use the values referenced in:
 * <p></p>
 * service_type_table ~ line 3990.
 */
public class ServiceTypeOptionValue implements OptionValueInformation {
  
  private static final Set<String> validOptions = ImmutableSet.of("simple", "forking", "oneshot", "dbus", "notify", "idle", "exec");
  
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
    return "config_parse_service_type";
  }
}
