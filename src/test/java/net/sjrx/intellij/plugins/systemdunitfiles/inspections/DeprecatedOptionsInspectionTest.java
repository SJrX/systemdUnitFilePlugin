package net.sjrx.intellij.plugins.systemdunitfiles.inspections;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.psi.PsiElement;
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest;

import java.util.List;

public class DeprecatedOptionsInspectionTest extends AbstractUnitFileTest {
  
  public void testNonDeprecatedOptionDoesNotThrowError() {
    String file = "[Service]\n"
                  + "ExecStart=/bin/bash";
    
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(DeprecatedOptionsInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(0, highlights);
  }
  
  public void testUnknownOptionDoesNotThrowError() {
    String file = "[Service]\n"
                  + "SomeOption=/bin/bash";
    
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(DeprecatedOptionsInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(0, highlights);
  }
  
  public void testUnknownSectionDoesNotThrowError() {
    String file = "[Servie]\n"
                  + "MemoryLimit=52";
    
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(DeprecatedOptionsInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(0, highlights);
  }
  
  public void testSingleExampleThrowsWarning() {
    String file = "[Service]\n"
                  + "MemoryLimit=8";
    
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(DeprecatedOptionsInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(1, highlights);
    HighlightInfo info = highlights.get(0);
    
    assertStringContains("Use MemoryMax= instead", info.getDescription());
    assertStringContains("deprecated", info.getDescription());
    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());
    
    assertEquals("MemoryLimit", highlightElement.getText());
  }

  public void testAllDocumentedDeprecatedOptionsInServiceAsOfV240ThrowsWarning() {
    String file = "[Service]\n"
                  + "CPUShares=52\n"
                  + "StartupCPUShares=25\n"
                  + "MemoryLimit=8\n"
                  + "BlockIOAccounting=1\n"
                  + "BlockIOWeight=25\n"
                  + "StartupBlockIOWeight=25\n"
                  + "BlockIODeviceWeight=25\n"
                  + "BlockIOReadBandwidth=2552\n"
                  + "BlockIOWriteBandwidth=252\n";
    
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(DeprecatedOptionsInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(9, highlights);
  }
  
  public void testAllDocumentedDeprecatedOptionsInMountAsOfV240ThrowsWarning() {
    String file = "[Mount]\n"
                  + "CPUShares=52\n"
                  + "StartupCPUShares=25\n"
                  + "MemoryLimit=8\n"
                  + "BlockIOAccounting=1\n"
                  + "BlockIOWeight=25\n"
                  + "StartupBlockIOWeight=25\n"
                  + "BlockIODeviceWeight=25\n"
                  + "BlockIOReadBandwidth=2552\n"
                  + "BlockIOWriteBandwidth=252\n";
    
    // Exercise SUT
    setupFileInEditor("file.mount", file);
    enableInspection(DeprecatedOptionsInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(9, highlights);
  }
  
  public void testAllDocumentedDeprecatedOptionsInSocketAsOfV240ThrowsWarning() {
    String file = "[Socket]\n"
                  + "CPUShares=52\n"
                  + "StartupCPUShares=25\n"
                  + "MemoryLimit=8\n"
                  + "BlockIOAccounting=1\n"
                  + "BlockIOWeight=25\n"
                  + "StartupBlockIOWeight=25\n"
                  + "BlockIODeviceWeight=25\n"
                  + "BlockIOReadBandwidth=2552\n"
                  + "BlockIOWriteBandwidth=252\n";
    
    // Exercise SUT
    setupFileInEditor("file.socket", file);
    enableInspection(DeprecatedOptionsInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(9, highlights);
  }
  
  public void testAllDocumentedDeprecatedOptionsInSwapAsOfV240ThrowsWarning() {
    String file = "[Swap]\n"
                  + "CPUShares=52\n"
                  + "StartupCPUShares=25\n"
                  + "MemoryLimit=8\n"
                  + "BlockIOAccounting=1\n"
                  + "BlockIOWeight=25\n"
                  + "StartupBlockIOWeight=25\n"
                  + "BlockIODeviceWeight=25\n"
                  + "BlockIOReadBandwidth=2552\n"
                  + "BlockIOWriteBandwidth=252\n";
    
    // Exercise SUT
    setupFileInEditor("file.swap", file);
    enableInspection(DeprecatedOptionsInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(9, highlights);
  }
  
  public void testAllDocumentedDeprecatedOptionsInSliceAsOfV240ThrowsWarning() {
    String file = "[Slice]\n"
                  + "CPUShares=52\n"
                  + "StartupCPUShares=25\n"
                  + "MemoryLimit=8\n"
                  + "BlockIOAccounting=1\n"
                  + "BlockIOWeight=25\n"
                  + "StartupBlockIOWeight=25\n"
                  + "BlockIODeviceWeight=25\n"
                  + "BlockIOReadBandwidth=2552\n"
                  + "BlockIOWriteBandwidth=252\n";
    
    // Exercise SUT
    setupFileInEditor("file.slice", file);
    enableInspection(DeprecatedOptionsInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(9, highlights);
  }
}
