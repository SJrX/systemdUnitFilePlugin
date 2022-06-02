package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues;

import java.util.Set;

public interface OptionValueInformation {
  
  /**
   * Returns the set of auto complete options for this option.
   */
  public Set<String> getAutoCompleteOptions();
  
  /**
   * Validates whether the value passed is valid.
   *
   * @param value - the value
   * @return <code>null</code> if the value is fine, some string describing the error if there is a problem.
   */
  public String getErrorMessage(String value);
  
  /**
   * Get the name of the validator that this implements.
   *
   * @return validator name
   */
  public String getValidatorName();
  
}
