package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata;


import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.*;

import java.util.Map;

public class SemanticDataRepositoryTest extends AbstractUnitFileTest {
  
  public void testInteresting() {
    SemanticDataRepository sdr = SemanticDataRepository.getInstance();
  
    assertInstanceOf(sdr.getOptionValidator("Socket", "SendSIGKILL"), BooleanOptionValue.class);
    
    assertInstanceOf(sdr.getOptionValidator("Unit", "Documentation"), DocumentationOptionValue.class);
  
    assertInstanceOf(sdr.getOptionValidator("Service", "KillMode"), KillModeOptionValue.class);
    assertInstanceOf(sdr.getOptionValidator("Mount", "KillMode"), KillModeOptionValue.class);

    assertInstanceOf(sdr.getOptionValidator("Socket", "DirectoryMode"), ModeStringOptionValue.class);


    assertInstanceOf(sdr.getOptionValidator("Unit", "XXXX"), NullOptionValue.class);
    assertInstanceOf(sdr.getOptionValidator("XXXX", "Yes"), NullOptionValue.class);

    assertInstanceOf(sdr.getOptionValidator("Service", "Restart"), RestartOptionValue.class);

    assertInstanceOf(sdr.getOptionValidator("Service", "Type"), ServiceTypeOptionValue.class);
  }

  public void testDeclaredUnderKeywordDiffers() {
    SemanticDataRepository sdr = SemanticDataRepository.getInstance();

    for (String section : sdr.getSectionNamesFromDocumentation()) {
      Map<String, SemanticDataRepository.KeywordData> data = sdr.getKeyValuePairsForSectionFromDocumentation(section);
      for (Map.Entry<String, SemanticDataRepository.KeywordData> entry : data.entrySet()) {
        String declaredUnderKeyword = entry.getValue().declaredUnderKeyword;
        if (declaredUnderKeyword != null && !declaredUnderKeyword.equals(entry.getKey())) {
          System.out.println("Mismatch: " + section + "." + entry.getKey() + ": " + declaredUnderKeyword);
        }
      }
    }
  }
}
