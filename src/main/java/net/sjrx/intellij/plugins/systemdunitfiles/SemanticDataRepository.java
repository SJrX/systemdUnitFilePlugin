package net.sjrx.intellij.plugins.systemdunitfiles;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class SemanticDataRepository {


  private static SemanticDataRepository instance = null;
  private final Map<String, Map<String, Map<String, String>>> sectionNameToKeyValues;

  /**
   * Default constructor.
   */
  private SemanticDataRepository() {

    URL sectionToKeywordMapJsonFile =
      this.getClass().getClassLoader().getResource("net/sjrx/intellij/plugins/systemdunitfiles/semanticdata/sectionToKeywordMap.json");

    final ObjectMapper mapper = new ObjectMapper();

    try {
      sectionNameToKeyValues =
        mapper.readValue(sectionToKeywordMapJsonFile, new TypeReference<Map<String, Map<String, Map<String, String>>>>() {
        });
    } catch (IOException e) {
      throw new IllegalStateException("Unable to initialize data for systemd inspections plugin", e);
    }
  }

  /**
   * Returns the allowed keywords by a given section name.
   *
   * @param section the section name (e.g., Unit, Install, Service)
   * @return set of allowed names.
   */
  public Set<String> getAllowedKeywordsInSection(String section) {
    return Collections.unmodifiableSet(this.getDataForSection(section).keySet());
  }

  private Map<String, Map<String, String>> getDataForSection(String section) {
    Map<String, Map<String, String>> sectionData = sectionNameToKeyValues.get(section);

    if (sectionData == null) {
      return Collections.emptyMap();
    } else {

      return sectionData;
    }
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
}
