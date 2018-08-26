package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues;

import java.util.Collections;
import java.util.Set;

/**
 * This validator is used for the config_parse_documentation validator.
 * <p></p>
 * While this is only use for one key, it is fairly popular in the source code, so we implemented it.
 * <p></p>
 * Source code is from
 * <p></p>
 * load-fragment.c ~ line 2600
 * web-util.c ~ line
 *
 */
public class DocumentationOptionValue implements OptionValueInformation {
  
  @Override
  public Set<String> getAutoCompleteOptions() {
    return Collections.emptySet();
  }
  
  @Override
  public boolean isValidValue(String value) {
    
    // This loose validation is what systemd does
    
    if (value.startsWith("http://")) {
      return true;
    }

    if (value.startsWith("https://")) {
      return true;
    }
  
    if (value.startsWith("file:/")) {
      return true;
    }
  
    if (value.startsWith("info:")) {
      return true;
    }
  
    if (value.startsWith("man:")) {
      return true;
    }
    
    return false;
  
  }
  
  @Override
  public String getValidatorName() {
    return "config_parse_documentation";
  }
}
