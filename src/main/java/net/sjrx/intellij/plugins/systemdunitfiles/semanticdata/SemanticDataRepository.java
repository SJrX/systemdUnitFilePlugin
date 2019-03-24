package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.diagnostic.Logger;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.BooleanOptionValue;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.DocumentationOptionValue;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.KillModeOptionValue;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ModeStringOptionValue;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.NullOptionValue;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.OptionValueInformation;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.RestartOptionValue;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ServiceTypeOptionValue;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemanticDataRepository {
  
  
  private static final Logger LOG = Logger.getInstance(SemanticDataRepository.class);
  private static final String SEMANTIC_DATA_ROOT = "net/sjrx/intellij/plugins/systemdunitfiles/semanticdata/";
  
  private static final String GPERF_REGEX = "^(?<Section>[A-Z][a-z]+).(?<Key>\\w+),\\s*(?<Validator>\\w+),\\s*(?<MysteryColumn>\\w+)\\s*,.+$";
  private static final Pattern LINE_MATCHER = Pattern.compile(GPERF_REGEX);
  private static final OptionValueInformation NULL_VALIDATOR = new NullOptionValue();
  
  
  private static SemanticDataRepository instance = null;
  private static final String SCOPE_KEYWORD = "Scope";
  private static final String LEGACY_PARAMETERS_KEY = "DISABLED_LEGACY";
  private static final String EXPERIMENTAL_PARAMETERS_KEY = "DISABLED_EXPERIMENTAL";
  
  private final Map<String, OptionValueInformation> validatorMap;
  private final Map</* Section */ String, Map</* Key */ String, /* Validator */ String>> sectionToKeyAndValidatorMap
    = new TreeMap<>();
  private final Map<String, Map<String, Map<String, String>>> sectionNameToKeyValuesFromDoc;
  private final Map<String, Map<String, Map<String, String>>> undocumentedOptionInfo;
  
  
  
  private SemanticDataRepository() {
    
    this.sectionNameToKeyValuesFromDoc = loadSemanticDataFromJsonFile(SEMANTIC_DATA_ROOT + "sectionToKeywordMapFromDoc.json");
  
    this.undocumentedOptionInfo = loadSemanticDataFromJsonFile(SEMANTIC_DATA_ROOT + "undocumentedSectionToKeywordMap.json");

    try (BufferedReader fr = new BufferedReader(new InputStreamReader(
      this.getClass().getClassLoader().getResourceAsStream(SEMANTIC_DATA_ROOT + "load-fragment-gperf.gperf")
    ))) {
      String line;
    
    
      while ((line = fr.readLine()) != null) {
      
        Matcher m = LINE_MATCHER.matcher(line);
      
        if (m.find()) {
          String section = m.group("Section");
          String key = m.group("Key");
          String validator = m.group("Validator");
          String mysteryValue = m.group("MysteryColumn");
  
          switch (mysteryValue) {
            case LEGACY_PARAMETERS_KEY:
            case EXPERIMENTAL_PARAMETERS_KEY:
              break;
    
            default:
              sectionToKeyAndValidatorMap.computeIfAbsent(section, k -> new TreeMap<>()).put(key, validator);
          }
          
        }
      }
    
      OptionValueInformation[] ovis = {new BooleanOptionValue(),
        new DocumentationOptionValue(),
        new KillModeOptionValue(),
        new ModeStringOptionValue(),
        new RestartOptionValue(),
        new ServiceTypeOptionValue(),
        NULL_VALIDATOR };
      validatorMap = new HashMap<>();
      
      for (OptionValueInformation ovi : ovis) {
        validatorMap.put(ovi.getValidatorName(), ovi);
      }
  
      /*
       * Scopes are not supported since they aren't standard unit files.
       */
      sectionNameToKeyValuesFromDoc.remove(SCOPE_KEYWORD);
      sectionToKeyAndValidatorMap.remove(SCOPE_KEYWORD);
      
    } catch (IOException e) {
      throw new IllegalStateException("Unable to initialize data for systemd inspections plugin", e);
    }
  }
  
  private Map<String, Map<String, Map<String, String>>> loadSemanticDataFromJsonFile(String filename) {
    Map<String, Map<String, Map<String, String>>> output;
    URL sectionToKeywordMapFromDocJsonFile =
      this.getClass().getClassLoader().getResource(filename);
    
    final ObjectMapper mapper = new ObjectMapper();
    
    try {
      output =
        mapper.readValue(sectionToKeywordMapFromDocJsonFile, new TypeReference<Map<String, Map<String, Map<String, String>>>>() {
        });
    } catch (IOException e) {
      throw new IllegalStateException("Unable to initialize data for systemd inspections plugin", e);
    }
    return output;
  }
  
  /**
   * Returns the allowed section names.
   *
   * @return a set of allow section names
   */
  public Set<String> getSectionNamesFromDocumentation() {
    return Collections.unmodifiableSet(sectionNameToKeyValuesFromDoc.keySet());
  }
  
  /**
   * Returns the allowed keywords by a given section name.
   *
   * @param section the section name (e.g., Unit, Install, Service)
   * @return set of allowed names.
   */
  public Set<String> getDocumentedKeywordsInSection(String section) {
    Set<String> keys = new HashSet<>(this.getKeyValuePairsForSectionFromDocumentation(section).keySet());
    
    keys.addAll(this.getKeyValuePairsForSectionFromUndocumentedInformation(section).keySet());
    
    return Collections.unmodifiableSet(keys);
  }
  
  /**
   * Returns the location (by keyword) for a specific keyword.
   * <p></p>
   * Some keys are documented together in systemd (e.g., After= and Before=), we can only
   * link to one of them, so this method will tell us what to link to.
   *
   * @param section - section name
   * @param keyName - key name
   * @return String or null
   */
  public String getKeywordLocationInDocumentation(String section, String keyName) {
    return this.getKeyValuePairsForSectionFromDocumentation(section).computeIfAbsent(keyName, k -> new HashMap<>())
             .computeIfAbsent("declaredUnderKeyword", v -> null);
  }
  
  /**
   * Returns the location in filename for a specific keyword.
   * <p></p>
   * Some keys are shared between many different types of units (e.g., WorkingDirectory= is in systemd.exec but shows up in [Service],
   * [Socket], etc...
   *
   * @param section - section name
   * @param keyName - key name
   * @return String or null
   */
  public String getKeywordFileLocationInDocumentation(String section, String keyName) {
    return this.getKeyValuePairsForSectionFromDocumentation(section).computeIfAbsent(keyName, k -> new HashMap<>())
             .computeIfAbsent("declaredInFile", v -> null);
  }
  
  private Map<String, Map<String, String>> getKeyValuePairsForSectionFromDocumentation(String section) {
    Map<String, Map<String, String>> sectionData = sectionNameToKeyValuesFromDoc.get(section);
    
    if (sectionData == null) {
      return Collections.emptyMap();
    } else {
      
      return sectionData;
    }
  }
  
  private Map<String, Map<String, String>> getKeyValuePairsForSectionFromUndocumentedInformation(String section) {
    Map<String, Map<String, String>> sectionData = undocumentedOptionInfo.get(section);
    
    if (sectionData == null) {
      return Collections.emptyMap();
    } else {
      
      return sectionData;
    }
  }
  
  /**
   * Returns a URL to the section name man page.
   *
   * @param sectionName - the name of the section name
   * @return - best URL for the section name or null if the section is unknown
   */
  public String getUrlForSectionName(String sectionName) {
    switch (sectionName) {
      case "Mount":
        return "https://www.freedesktop.org/software/systemd/man/systemd.mount.html";
      case "Automount":
        return "https://www.freedesktop.org/software/systemd/man/systemd.automount.html";
      case "Install":
        return "https://www.freedesktop.org/software/systemd/man/systemd.unit.html#%5BInstall%5D%20Section%20Options";
      case "Path":
        return "https://www.freedesktop.org/software/systemd/man/systemd.path.html";
      case "Service":
        return "https://www.freedesktop.org/software/systemd/man/systemd.service.html";
      case "Slice":
        return "https://www.freedesktop.org/software/systemd/man/systemd.slice.html";
      case "Socket":
        return "https://www.freedesktop.org/software/systemd/man/systemd.socket.html";
      case "Swap":
        return "https://www.freedesktop.org/software/systemd/man/systemd.swap.html";
      case "Timer":
        return "https://www.freedesktop.org/software/systemd/man/systemd.timer.html";
      case "Unit":
        return "https://www.freedesktop.org/software/systemd/man/systemd.unit.html#%5BUnit%5D%20Section%20Options";
      default:
        return null;
    }
  }
  
  /**
   * Returns a document blurb for a Section.
   *
   * @param sectionName the name of the section.
   * @return string
   */
  public String getDocumentationContentForSection(String sectionName) {
    switch (sectionName) {
      case "Mount":
        return " Mount files must include a [Mount] section, which carries information\n"
               + "       about the file system mount points it supervises. A number of options\n"
               + "       that may be used in this section are shared with other unit types.\n"
               + "       These options are documented in <a href=\"http://man7.org/linux/man-pages/man5/systemd.exec.5.html\">"
               + "systemd.exec(5)</a> and <a href=\"http://man7.org/linux/man-pages/man5/systemd.kill.5.html\">systemd.kill(5)</a>.";
      
      case "Automount":
        return "Automount files must include an [Automount] section, which carries\n"
               + "       information about the file system automount points it supervises.";
      
      case "Install":
        return "Unit files may include an \"[Install]\" section, which carries\n"
               + "       installation information for the unit. This section is not\n"
               + "       interpreted by <a href=\"http://man7.org/linux/man-pages/man1/systemd.1.html\">systemd(1)</a> during runtime; it is"
               + "       used by the <b>enable</b> and <b>disable </b>commands of the"
               + "       <a href=\"http://man7.org/linux/man-pages/man1/systemctl.1.html\">systemctl(1)</a> tool during installation of\n"
               + "       a unit. ";
      case "Path":
        return "Path files must include a [Path] section, which carries information\n"
               + "       about the path(s) it monitors";
      
      case "Service":
        return "Service files must include a \"[Service]\" section, which carries\n"
               + "information about the service and the process it supervises. A number\n"
               + "of options that may be used in this section are shared with other\n"
               + "unit types. These options are documented in <a href=\"http://man7.org/linux/man-pages/man5/systemd.exec.5.html\">systemd.exec(5)</a>,\n"
               + "<a href=\"http://man7.org/linux/man-pages/man5/systemd.kill.5.html\">systemd.kill(5)</a> and "
               + "<a href=\"http://man7.org/linux/man-pages/man5/systemd.resource-control.5.html\">systemd.resource-control(5)</a>.";
      case "Slice":
        return "The slice specific configuration\n"
               + "       options are configured in the [Slice] section. Currently, only\n"
               + "       generic resource control settings as described in\n"
               + "       <a href=\"http://man7.org/linux/man-pages/man5/systemd.resource-control.5.html\">systemd.resource-control(5)</a> are allowed.\n";
      
      
      case "Socket":
        return "Socket files must include a [Socket] section, which carries\n"
               + "       information about the socket or FIFO it supervises. A number of\n"
               + "       options that may be used in this section are shared with other unit\n"
               + "       types. These options are documented in <a href=\"http://man7.org/linux/man-pages/man5/systemd.exec.5.html\">"
               + "       systemd.exec(5)</a> and <a href=\"http://man7.org/linux/man-pages/man5/systemd.kill.5.html\">systemd.kill(5)</a>.";
      case "Swap":
        return "Swap files must include a [Swap] section, which carries information\n"
               + " about the swap device it supervises. A number of options that may be\n"
               + " used in this section are shared with other unit types. These options\n"
               + " are documented in <a href=\"http://man7.org/linux/man-pages/man5/systemd.exec.5.html\">systemd.exec(5)</a>"
               + " and <a href=\"http://man7.org/linux/man-pages/man5/systemd.kill.5.html\">systemd.kill(5)</a>";
      
      case "Timer":
        return " Timer files must include a [Timer] section, which carries information\n"
               + " about the timer it defines.";
      
      case "Unit":
        return "The unit file may include a [Unit] section, which carries generic\n"
               + " information about the unit that is not dependent on the type of unit.";
      
      default:
        return null;
    }
  }
  
  /**
   * Return the documentation for this key.
   *
   * @param sectionName the section name to lookup (e.g., Unit, Install, Service)
   * @param keyName     the key name to look up
   * @return either the first paragraph from the HTML description or null if no description was found
   */
  public String getDocumentationContentForKeyInSection(String sectionName, String keyName) {
    
    InputStream htmlDocStream = this.getClass().getClassLoader().getResourceAsStream(SEMANTIC_DATA_ROOT + "/documents/completion/"
                                                                                     + sectionName + "/" + keyName + ".html");
    if (htmlDocStream == null) {
      
      Map<String, String> options = this.getKeyValuePairsForSectionFromUndocumentedInformation(sectionName).get(keyName);
  
      if (options == null) {
        return null;
      }
  
      switch (options.get("reason")) {
        case "unsupported":
          return "<p><var>" + keyName + "</var> in section <b>" + sectionName + "</b> is not officially supported.<p>"
                 + "<a href='" + options.get("documentationLink") + "'>More information is available here</a>";
        case "moved":
          return "<p>The key <var>" + keyName + "</var> in section <b>" + sectionName + "</b> has been moved to "
                 + "<var>" + options.getOrDefault("replacedWithKey", keyName) + "</var> in section <b>"
                 + options.getOrDefault("replacedWithSection", sectionName) + "</b>"
                 + "<p>NOTE: The semantics of the new value may not match the existing value.<p>"
                 + "<a href='" + options.get("documentationLink") + "'>More information is available here</a>";
        default:
          LOG.warn("Found unsupported " + sectionName + " => " + keyName);
          return null;
      }
    }
    
    try {
      return IOUtils.toString(htmlDocStream, "UTF-8");
    } catch (IOException e) {
      LOG.warn("Could not convert html document stream to String", e);
    }
    return null;
  }
  
  /**
   * Checks whether the option is deprecated or unsupported.
   *
   * @param sectionName the section name to lookup (e.g., Unit, Install, Service)
   * @param keyName the key name to look up.
   * @return true if we know the option is deprecated
   */
  public boolean isDeprecated(String sectionName, String keyName) {
    Map<String, String> options = this.getKeyValuePairsForSectionFromUndocumentedInformation(sectionName).get(keyName);
  
    return options != null;
  }
  
  /**
   * Gets the validator for a section name and key name.
   *
   * @param sectionName the name of the section we are looking up
   * @param keyName - the keyname to look up
   * @return the validator
   */
  public OptionValueInformation getOptionValidator(String sectionName, String keyName) {
    String validatorName = sectionToKeyAndValidatorMap.getOrDefault(sectionName, Collections.emptyMap()).get(keyName);
    
    return validatorMap.getOrDefault(validatorName, NULL_VALIDATOR);
  }
  
  
  
  /**
   * Gets the Semantic Data Repository.
   *
   * @return singleton instance
   */
  public static synchronized SemanticDataRepository getInstance() {
    if (instance == null) {
      instance = new SemanticDataRepository();
    }
    
    return instance;
  }
  
  /**
   * Return the section names from the validators.
   *
   * @return set
   */
  public Set<String> getSectionNamesFromValidators() {
    return Collections.unmodifiableSet(this.sectionToKeyAndValidatorMap.keySet());
  }
  
  /**
   * Return the section names from the validators.
   *
   * @return set
   */
  public Set<String> getAllowedKeywordsInSectionFromValidators(String sectionName) {
    return Collections.unmodifiableSet(this.sectionToKeyAndValidatorMap.getOrDefault(sectionName, Collections.emptyMap()).keySet());
  }
  
}
