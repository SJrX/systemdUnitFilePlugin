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
  public String getErrorMessage(String values) {
  
    // This loose validation is what systemd does
    for (String value : values.split("\\s+")) {
      if (value.startsWith("http://")) {
        continue;
      }
    
      if (value.startsWith("https://")) {
        continue;
      }
    
      if (value.startsWith("file:/")) {
        continue;
      }
    
      if (value.startsWith("info:")) {
        continue;
      }
    
      if (value.startsWith("man:")) {
        continue;
      }
  
      return "Documentation " + value + " does not match expected syntax, each value should be a space separated list beginning with (http://, https://, file:/, info:, man:)";
    }

    return null;
  }
  
  @Override
  public String getValidatorName() {
    return "config_parse_documentation";
  }
}
