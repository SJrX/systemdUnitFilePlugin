package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues;

import java.util.Collections;
import java.util.Set;

/**
 * Matches a mode value written in octal.
 * <p></p>
 * Looks like this was done internally in C, but test-conf-parser.c ~ line 172
 * has some stuff.
 */
public class ModeStringOptionValue implements OptionValueInformation {
  @Override
  public Set<String> getAutoCompleteOptions() {
    return Collections.emptySet();
  }
  
  @Override
  public String getErrorMessage(String value) {
    if (value.matches("[0-7]{3,4}")) {
      return null;
    } else {
      return "Value is expected to be a 3 or 4 digit octal number not: " + value;
    }
  }
  
  @Override
  public String getValidatorName() {
    return "config_parse_mode";
  }
}
