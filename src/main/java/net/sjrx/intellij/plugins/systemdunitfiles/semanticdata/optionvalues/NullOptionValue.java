package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues;

import java.util.Collections;
import java.util.Set;

/**
 * This validator has no auto completion and marks everything as valid.
 */
public class NullOptionValue implements OptionValueInformation {
  
  @Override
  public Set<String> getAutoCompleteOptions() {
    return Collections.emptySet();
  }
  
  @Override
  public boolean isValidValue(String value) {
    return true;
  }
  
  @Override
  public String getValidatorName() {
    return "NULL";
  }
}
