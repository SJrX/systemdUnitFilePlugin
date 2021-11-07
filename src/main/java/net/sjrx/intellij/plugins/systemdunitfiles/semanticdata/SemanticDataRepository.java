package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ObjectUtils;
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
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemanticDataRepository {


  private static final Logger LOG = Logger.getInstance(SemanticDataRepository.class);
  private static final String SEMANTIC_DATA_ROOT = "net/sjrx/intellij/plugins/systemdunitfiles/semanticdata/";

  private static final String GPERF_REGEX = "^(?<Section>[A-Z][a-z]+).(?<Key>\\w+),\\s*(?<Validator>\\w+),\\s*(?<MysteryColumn>\\w+)\\s*,.+$";
  private static final Pattern LINE_MATCHER = Pattern.compile(GPERF_REGEX);
  private static final OptionValueInformation NULL_VALIDATOR = new NullOptionValue();


  private static final NotNullLazyValue<SemanticDataRepository> instance = NotNullLazyValue.atomicLazy(SemanticDataRepository::new);
  
  private static final String SCOPE_KEYWORD = "Scope";
  private static final String LEGACY_PARAMETERS_KEY = "DISABLED_LEGACY";
  private static final String EXPERIMENTAL_PARAMETERS_KEY = "DISABLED_EXPERIMENTAL";

  private final Map<String, OptionValueInformation> validatorMap;
  private final Map</* Section */ String, Map</* Key */ String, /* Validator */ String>> sectionToKeyAndValidatorMap
          = new HashMap<>();
  private final Map<String, Map<String, KeywordData>> sectionNameToKeyValuesFromDoc;
  private final Map<String, Map<String, KeywordData>> undocumentedOptionInfo;

  public static final class KeywordData {
    public String declaredUnderKeyword;
    public String declaredInFile;
    public String reason;
    public String documentationLink;
    public String replacedWithKey;
    public String replacedWithSection;
    public String description;

    public String values;
    public String unsupported;
  }

  private SemanticDataRepository() {
    Map<String, String> cache = new HashMap<>();

    this.sectionNameToKeyValuesFromDoc = loadSemanticDataFromJsonFile(SEMANTIC_DATA_ROOT + "sectionToKeywordMapFromDoc.json", cache);

    this.undocumentedOptionInfo = loadSemanticDataFromJsonFile(SEMANTIC_DATA_ROOT + "undocumentedSectionToKeywordMap.json", cache);

    validatorMap = new HashMap<>();

    InputStream resource = this.getClass().getClassLoader().getResourceAsStream(SEMANTIC_DATA_ROOT + "load-fragment-gperf.gperf");
    if (resource != null) {
      try (BufferedReader fr = new BufferedReader(new InputStreamReader(resource))) {
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
                sectionToKeyAndValidatorMap.computeIfAbsent(intern(cache, section), k -> new HashMap<>()).put(intern(cache, key), intern(cache, validator));
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

        for (OptionValueInformation ovi : ovis) {
          validatorMap.put(ovi.getValidatorName(), ovi);
        }

        /*
         * Scopes are not supported since they aren't standard unit files.
         */
        sectionNameToKeyValuesFromDoc.remove(SCOPE_KEYWORD);
        sectionToKeyAndValidatorMap.remove(SCOPE_KEYWORD);

      } catch (IOException e) {
        LOG.error("Unable to initialize data for systemd inspections plugin", e);
      }
    } else {
      LOG.error("Unable to initialize data for systemd inspections plugin, resource not found");
    }
  }

  private static String intern(Map<String, String> cache, String value) {
    return cache.getOrDefault(value, value);
  }

  private Map<String, Map<String, KeywordData>> loadSemanticDataFromJsonFile(String filename, Map<String, String> cache) {
    URL sectionToKeywordMapFromDocJsonFile =
            this.getClass().getClassLoader().getResource(filename);

    final JsonFactory factory = new JsonFactoryBuilder().disable(JsonFactory.Feature.INTERN_FIELD_NAMES).build();
    
    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      factory.disable(JsonParser.Feature.IGNORE_UNDEFINED);
    }
    final ObjectMapper mapper = new ObjectMapper(factory);

    Map<String, Map<String, KeywordData>> read;
    try {
      read = mapper.readValue(sectionToKeywordMapFromDocJsonFile, new TypeReference<Map<String, Map<String, KeywordData>>>() {
      });
    } catch (IOException e) {
      LOG.error("Unable to initialize data for systemd inspections plugin", e);
      read = new HashMap<>();
    }
    // intern strings in maps
    final Map<String, Map<String, KeywordData>> output = new HashMap<>();
    read.forEach((section, m1) -> {
      HashMap<String, KeywordData> converted = new HashMap<>();
      m1.forEach((keyword, m2) -> converted.put(intern(cache, keyword), m2));
      output.put(intern(cache, section), converted);
    });
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

  public String getKeywordDocumentationUrl(String section, String keyName) {
    KeywordData data = this.getKeyValuePairsForSectionFromDocumentation(section).get(keyName);
    if (data != null && data.documentationLink != null) {
      return data.documentationLink;
    }
    data = this.getKeyValuePairsForSectionFromUndocumentedInformation(section).get(keyName);
    if (data != null && data.documentationLink != null) {
      return data.documentationLink;
    }
    return null;
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
    KeywordData data = this.getKeyValuePairsForSectionFromDocumentation(section).get(keyName);
    if (data == null) return null;
    return ObjectUtils.notNull(data.declaredUnderKeyword, keyName);
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
    KeywordData data = this.getKeyValuePairsForSectionFromDocumentation(section).get(keyName);
    return data != null ? data.declaredInFile : null;
  }

  Map<String, KeywordData> getKeyValuePairsForSectionFromDocumentation(String section) {
    Map<String, KeywordData> sectionData = sectionNameToKeyValuesFromDoc.get(section);
  
    return Objects.requireNonNullElse(sectionData, Collections.emptyMap());
  }

  public Map<String, KeywordData> getKeyValuePairsForSectionFromUndocumentedInformation(String section) {
    Map<String, KeywordData> sectionData = undocumentedOptionInfo.get(section);
  
    return Objects.requireNonNullElse(sectionData, Collections.emptyMap());
  }

  /**
   * Returns a URL to the section name man page.
   *
   * @param sectionName - the name of the section name
   * @return best URL for the section name or null if the section is unknown
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
      return getDeprecationReason(sectionName, keyName, true);
    }

    try {
      return IOUtils.toString(htmlDocStream, StandardCharsets.UTF_8);
    } catch (IOException e) {
      LOG.warn("Could not convert html document stream to String", e);
    }
    return null;
  }

  public String getDeprecationReason(String sectionName, String keyName, boolean html) {
    KeywordData options = this.getKeyValuePairsForSectionFromUndocumentedInformation(sectionName).get(keyName);

    if (options == null) return null;

    switch (options.reason) {
      case "unsupported":
        if (!html) return String.format("'%s' in section '%s' is not officially supported", keyName, sectionName);
        return "<p><var>" + keyName + "</var> in section <b>" + sectionName + "</b> is not officially supported.<p>"
                + "<a href='" + options.documentationLink + "'>More information is available here</a>";
      case "moved":
        String newKeyName = StringUtil.notNullize(options.replacedWithKey, keyName);
        String newSectionName = StringUtil.notNullize(options.replacedWithSection, sectionName);
        String semanticsNote = !html ? "" : "<p>NOTE: The semantics of the new value may not match the existing value.<p>"
                + "<a href='" + options.documentationLink + "'>More information is available here</a>";

        if (newSectionName.equals(sectionName)) {
          LOG.assertTrue(!newKeyName.equals(keyName), String.format("Meaningless move/rename of %s.%s", sectionName, keyName));
          if (!html) {
            return String.format("'%s' in section '%s' has been renamed to '%s'", keyName, sectionName, newKeyName);
          }
          return "<p>The key <var>" + keyName + "</var> in section <b>" + sectionName + "</b> has been renamed to "
                  + "<var>" + newKeyName + "</var>" + semanticsNote;
        }
        if (newKeyName.equals(keyName)) {
          if (!html) {
            return String.format("'%s' has been moved to section '%s'", keyName, newSectionName);
          }
          return "<p>The key <var>" + keyName + "</var> in section <b>" + sectionName + "</b> has been moved to "
                  + "section <b>" + newSectionName + "</b>" + semanticsNote;
        }
        if (!html) {
          return String.format("'%s' in section '%s' has been moved to '%s' in section '%s'", keyName, sectionName, newKeyName, newSectionName);
        }
        return "<p>The key <var>" + keyName + "</var> in section <b>" + sectionName + "</b> has been moved to "
                + "<var>" + newKeyName + "</var> in section <b>" + newSectionName + "</b>"
                + semanticsNote;
      case "manual":
        if (!html) return StringUtil.stripHtml(options.description, false);
        return options.description;
      default:
        LOG.warn("Found deprecated key '" + sectionName + "." + keyName + "' with unclear reason: " + options.reason);
        return null;
    }
  }

  /**
   * Checks whether the option is deprecated or unsupported.
   *
   * @param sectionName the section name to lookup (e.g., Unit, Install, Service)
   * @param keyName the key name to look up.
   * @return true if we know the option is deprecated
   */
  public boolean isDeprecated(String sectionName, String keyName) {
    KeywordData options = this.getKeyValuePairsForSectionFromUndocumentedInformation(sectionName).get(keyName);
  
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
  public static SemanticDataRepository getInstance() {
    return instance.getValue();
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
