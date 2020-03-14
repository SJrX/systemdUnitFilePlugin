package net.sjrx.intellij.plugins.systemdunitfiles.inspections;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.psi.PsiElement;
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest;

import java.util.List;

public class InvalidValueInspectionTest extends AbstractUnitFileTest {
  
  public void testAllValidBooleanValuesDoNotThrowError() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "TTYReset=1\n"
                  + "TTYReset=yes\n"
                  + "TTYReset=y\n"
                  + "TTYReset=true\n"
                  + "TTYReset=t\n"
                  + "TTYReset=on\n"
                  + "TTYReset=0\n"
                  + "TTYReset=no\n"
                  + "TTYReset=n\n"
                  + "TTYReset=false\n"
                  + "TTYReset=f\n"
                  + "TTYReset=off\n";
    
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(InvalidValueInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
  
  
    // Verification
    assertSize(0, highlights);
  }
  
  public void testIllegalBooleanValueTriggersInspection() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "TTYReset=Most Def\n";
    
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(InvalidValueInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(1, highlights);
    HighlightInfo info = highlights.get(0);
    
    assertStringContains("Most Def", info.getDescription());
    assertStringContains("must be one of", info.getDescription());
    assertStringContains("on", info.getDescription());
    assertStringContains("off", info.getDescription());
    
    assertEquals(HighlightInfoType.WARNING, info.type);
  
    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertNotNull(highlightElement);
    assertEquals("Most Def", highlightElement.getText());
    
  }
  
  public void testValidDocumentationValuesOnDistinctLinesDoNotThrowException() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Documentation=http://www.google.com\n"
                  + "Documentation=https://www.google.com\n"
                  + "Documentation=file:/tmp/test.txt\n"
                  + "Documentation=info:gcc#G++_and_GCC\n"
                  + "Documentation=man:test(7)\n";
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(InvalidValueInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(0, highlights);
  }
  
  public void testValidDocumentationValuesOnSingleLineDoNotThrowException() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Documentation=http://www.google.com https://www.google.com file:/tmp/test.txt info:gcc#G++_and_GCC man:test(7)\n";
      
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(InvalidValueInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertSize(0, highlights);
  }
  
  public void testInvalidDocumentationValuesOnDistinctLinesTriggersInspection() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Documentation=http://www.google.com\n"
                  + "Documentation=https://www.google.com\n"
                  + "Documentation=file:/tmp/test.txt\n"
                  + "Documentation=ftp://www.google.com/rules.txt\n"
                  + "Documentation=info:gcc#G++_and_GCC\n"
                  + "Documentation=man:test(7)\n";
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(InvalidValueInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(1, highlights);
    HighlightInfo info = highlights.get(0);
  
    assertStringContains("ftp://www.google.com/rules.txt", info.getDescription());
    assertStringContains("a space separated list", info.getDescription());
  
    assertEquals(HighlightInfoType.WARNING, info.type);
  
    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertNotNull(highlightElement);
    assertEquals("ftp://www.google.com/rules.txt", highlightElement.getText());
  }
  
  public void testInvalidDocumentationValuesOnSingleLineTriggersInspection() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Documentation=http://www.google.com https://www.google.com file:/tmp/test.txt ftp://www.google.com/rules.txt"
                  + " info:gcc#G++_and_GCC man:test(7)\n";
    
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(InvalidValueInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    // Verification
    assertSize(1, highlights);
    HighlightInfo info = highlights.get(0);
  
    assertStringContains("ftp://www.google.com/rules.txt", info.getDescription());
    assertStringContains("a space separated list", info.getDescription());
  
    assertEquals(HighlightInfoType.WARNING, info.type);
  
    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertNotNull(highlightElement);
    assertStringContains("ftp://www.google.com/rules.txt", highlightElement.getText());
  }
  
  public void testAllKillModeValuesDoNotThrowError() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "KillMode=control-group\n"
                  + "KillMode=process\n"
                  + "KillMode=mixed\n"
                  + "KillMode=none\n";

    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(InvalidValueInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(0, highlights);
  }
  
  public void testIllegalKillModeValueTriggersInspection() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "KillMode=sigkill\n";
    
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(InvalidValueInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(1, highlights);
    HighlightInfo info = highlights.get(0);
    
    assertStringContains("sigkill", info.getDescription());
    assertStringContains("match one of", info.getDescription());
    assertStringContains("control-group", info.getDescription());
    assertStringContains("process", info.getDescription());
    assertStringContains("mixed", info.getDescription());
    assertStringContains("none", info.getDescription());
    
    assertEquals(HighlightInfoType.WARNING, info.type);
    
    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());
    assertNotNull(highlightElement);
    assertEquals("sigkill", highlightElement.getText());
  }
  
  public void testAllRestartOptionValuesDoNotThrowError() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "Restart=no\n"
                  + "Restart=always\n"
                  + "Restart=on-success\n"
                  + "Restart=on-failure\n"
                  + "Restart=on-abnormal\n"
                  + "Restart=on-abort\n"
                  + "Restart=on-watchdog\n"
                  + "Restart=on-watchdog";
      
    
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(InvalidValueInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(0, highlights);
  }
  
  public void testIllegalRestartOptionValueTriggersInspection() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "Restart=yes\n";
    
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(InvalidValueInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(1, highlights);
    HighlightInfo info = highlights.get(0);
    
    assertStringContains("yes", info.getDescription());
    assertStringContains("match one of", info.getDescription());
    assertStringContains("no", info.getDescription());
    assertStringContains("on-success", info.getDescription());
    assertStringContains("on-failure", info.getDescription());
    assertStringContains("on-abnormal", info.getDescription());
    assertStringContains("on-watchdog", info.getDescription());
    assertStringContains("on-abort", info.getDescription());
    assertStringContains("always", info.getDescription());
    
    
    assertEquals(HighlightInfoType.WARNING, info.type);
    
    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());
    assertNotNull(highlightElement);
    assertEquals("yes", highlightElement.getText());
  }
  
  public void testValidModeStringOptionsDoNotTriggerInspection() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "UMask=0000\n"
                  + "UMask=0111\n"
                  + "UMask=111\n"
                  + "UMask=0777\n"
                  + "UMask=4777\n"
                  + "UMask=2777\n"
                  + "UMask=666\n";
    
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(InvalidValueInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(0, highlights);
  }
  
  public void testIllegalModeStringOptionsTriggersInspection() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "UMask=rwxrwxrwx\n"
                  + "UMask=u=rwx,g=rwx,o=rwx\n"
                  + "UMask=0A548\n"
                  + "UMask=0897\n"
                  + "UMask=71777\n"
                  + "UMask=71\n";
    
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(InvalidValueInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(6, highlights);
  
    for (HighlightInfo info : highlights) {
      assertStringContains("Value is expected to be a 3 or 4 digit octal number not:", info.getDescription());
      assertEquals(HighlightInfoType.WARNING, info.type);
      PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());
      assertNotNull(highlightElement);
      assertStringContains(highlightElement.getText().trim(), info.getDescription());
    }
  }
  
  public void testAllServiceTypesValuesDoNotThrowError() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "Type=simple\n"
                  + "Type=forking\n"
                  + "Type=oneshot\n"
                  + "Type=dbus\n"
                  + "Type=notify\n"
                  + "Type=idle\n"
                  + "Type=exec\n";
    
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(InvalidValueInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(0, highlights);
  }
  
  public void testIllegalServiceTypeValueTriggersInspectionWithNewLine() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "Type=remote\n";
    
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(InvalidValueInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(1, highlights);
    HighlightInfo info = highlights.get(0);
    
    assertStringContains("remote", info.getDescription());
    assertStringContains("match one of", info.getDescription());
    assertStringContains("simple", info.getDescription());
    assertStringContains("forking", info.getDescription());
    assertStringContains("oneshot", info.getDescription());
    assertStringContains("dbus", info.getDescription());
    assertStringContains("notify", info.getDescription());
    assertStringContains("idle", info.getDescription());
    assertStringContains("exec", info.getDescription());
    
    
    assertEquals(HighlightInfoType.WARNING, info.type);
    
    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());
    assertNotNull(highlightElement);
    assertEquals("remote", highlightElement.getText());
  }
  
  public void testIllegalServiceTypeValueTriggersInspectionWithoutNewLine() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "Type=remote\n";
    
    // Exercise SUT
    setupFileInEditor("file.service", file);
    enableInspection(InvalidValueInspection.class);
    
    // Verification
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(1, highlights);
    HighlightInfo info = highlights.get(0);
    
    assertStringContains("remote", info.getDescription());
    assertStringContains("match one of", info.getDescription());
    assertStringContains("simple", info.getDescription());
    assertStringContains("forking", info.getDescription());
    assertStringContains("oneshot", info.getDescription());
    assertStringContains("dbus", info.getDescription());
    assertStringContains("notify", info.getDescription());
    assertStringContains("idle", info.getDescription());
    assertStringContains("exec", info.getDescription());
    
    
    assertEquals(HighlightInfoType.WARNING, info.type);
    
    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());
    assertNotNull(highlightElement);
    assertEquals("remote", highlightElement.getText());
  }
}
