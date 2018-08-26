package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata;


import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.BooleanOptionValue;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.DocumentationOptionValue;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.KillModeOptionValue;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ModeStringOptionValue;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.NullOptionValue;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.RestartOptionValue;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ServiceTypeOptionValue;

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
}
