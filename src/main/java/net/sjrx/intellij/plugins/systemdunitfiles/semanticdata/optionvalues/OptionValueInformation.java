package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues;

import java.util.Set;

public interface OptionValueInformation {
  
  /**
   * Returns the set of auto complete options for this option.
   */
  public Set<String> getAutoCompleteOptions();
  
  /**
   * Validates whether or not the value passed is valid.
   *
   * @param value - the value
   * @return <code>true</code> if the value is valid, <code>false</code> otherwise.
   */
  public boolean isValidValue(String value);
  
  /**
   * Get the name of the validator that this implements.
   *
   * @return validator name
   */
  public String getValidatorName();
  
}
