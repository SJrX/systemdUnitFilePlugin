package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata;

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest;

import java.util.Set;
import java.util.TreeSet;

public class SemanticDataDocumentationCompletionTest extends AbstractUnitFileTest  {
  
  
  public void testAllOptions() {
    SemanticDataRepository sdr = SemanticDataRepository.getInstance();
    
    Set<String> doc = new TreeSet<>();
    for (String sectionName : sdr.getSectionNamesFromDocumentation()) {
      for (String keyName : sdr.getDocumentedKeywordsInSection(sectionName)) {
        doc.add(sectionName + "." + keyName);
      }
    }
  
    Set<String> code = new TreeSet<>();
    for (String sectionName : sdr.getSectionNamesFromValidators()) {
      for (String keyName : sdr.getAllowedKeywordsInSectionFromValidators(sectionName)) {
        code.add(sectionName + "." + keyName);
      }
    }
  
    
    
    
    System.out.println(doc.size());
    System.out.println(code.size());
  
    Set<String> codeButNotDoc = new TreeSet<>(code);
    
    Set<String> docButNotCode = new TreeSet<>(code);

    codeButNotDoc.removeAll(doc);

    docButNotCode.removeAll(code);
    System.err.println("***** (Code but not doc) *****");
    System.err.flush();
    
    assertEmpty("Expected that everything in the code was in the documentation, but we are missing the following:", codeButNotDoc);
    assertEmpty("expected that everything in the documentation was in the code, but we have documentation for the following unknown thingies: ", docButNotCode);
  }
}

