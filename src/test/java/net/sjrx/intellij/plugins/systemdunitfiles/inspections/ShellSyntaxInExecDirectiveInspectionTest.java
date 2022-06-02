package net.sjrx.intellij.plugins.systemdunitfiles.inspections;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest;

import java.util.List;

public class ShellSyntaxInExecDirectiveInspectionTest extends AbstractUnitFileTest {
  
  public void testValidSimpleExecCallDoesNotThrowError() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "ExecStart=docker-compose";
    
    // Execute SUT
    setupFileInEditor("file.service", file);
    enableInspection(ShellSyntaxInExecDirectiveInspection.class);
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    // Verification
    assertSize(0, highlights);
  }
  
  public void testValidMultipleArgumentExecCallDoesNotGenerateError() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "ExecStop=docker-compose -f docker-compose.yml down";
    
    // Execute SUT
    setupFileInEditor("file.service", file);
    enableInspection(ShellSyntaxInExecDirectiveInspection.class);
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    // Verification
    assertSize(0, highlights);
  }
  
  public void testRandomOptionDoesNotGenerateError() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "OnSuccessMode=docker-compose -f docker-compose.yml down > log.txt";
    
    // Execute SUT
    setupFileInEditor("file.service", file);
    enableInspection(ShellSyntaxInExecDirectiveInspection.class);
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    // Verification
    assertSize(0, highlights);
  }
  
  public void testSimpleExecCallWithRedirectDoesThrowError() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "ExecStart=docker-compose>file.txt";
    
    // Execute SUT
    setupFileInEditor("file.service", file);
    enableInspection(ShellSyntaxInExecDirectiveInspection.class);
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    // Verification
    assertSize(1, highlights);
  }
  
  public void testSimpleExecCallWithTwoSpecialCharactersGeneratesTwoErrors() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "ExecStart=docker-compose < hello.txt > file.txt";
    
    // Execute SUT
    setupFileInEditor("file.service", file);
    enableInspection(ShellSyntaxInExecDirectiveInspection.class);
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    // Verification
    assertSize(2, highlights);
  }
  
  public void testSimpleExecCallWithAppendSpecialCharactersOnlyHasTwoErrors() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "ExecStart=docker-compose << hello.txt >> file.txt";
    
    // Execute SUT
    setupFileInEditor("file.service", file);
    enableInspection(ShellSyntaxInExecDirectiveInspection.class);
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    // Verification
    assertSize(2, highlights);
  }
  
  public void testSimpleExecCallWithPipeHasError() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "ExecStart=docker-compose | grep foo";
    
    // Execute SUT
    setupFileInEditor("file.service", file);
    enableInspection(ShellSyntaxInExecDirectiveInspection.class);
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    // Verification
    assertSize(1, highlights);
  }
  
  public void testSimpleExecCallWithBackgroundHasError() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "ExecStart=docker-compose up --build&";
    
    // Execute SUT
    setupFileInEditor("file.service", file);
    enableInspection(ShellSyntaxInExecDirectiveInspection.class);
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    // Verification
    assertSize(1, highlights);
  }
  
  public void testWrappedCallWithDoubleQuotesAllMetaCharactersHasNoErrors() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "ExecStart=/bin/sh -c \"a < b << c > d >> e | f&\"\n";
    
    // Execute SUT
    setupFileInEditor("file.service", file);
    enableInspection(ShellSyntaxInExecDirectiveInspection.class);
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    // Verification
    assertSize(0, highlights);
  }
  
  public void testWrappedCallWithSingleQuotesAllMetaCharactersHasNoErrors() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "ExecStart=/bin/sh -c 'a < b << c > d >> e | f&'\n";
    
    // Execute SUT
    setupFileInEditor("file.service", file);
    enableInspection(ShellSyntaxInExecDirectiveInspection.class);
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    // Verification
    assertSize(0, highlights);
  }
  
  
  
  
  
  
}
