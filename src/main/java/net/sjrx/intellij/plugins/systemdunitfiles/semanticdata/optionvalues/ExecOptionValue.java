package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues;

import java.util.Collections;
import java.util.Set;

/**
 * This validator is used for the config_parse_exec validator.
 * <p/>
 * This validator does nothing because other inspections use it to determine if they should run,
 * kind of hacky I know.
 */
public class ExecOptionValue implements OptionValueInformation {
  
  @Override
  public Set<String> getAutoCompleteOptions() {
    return Collections.emptySet();
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
