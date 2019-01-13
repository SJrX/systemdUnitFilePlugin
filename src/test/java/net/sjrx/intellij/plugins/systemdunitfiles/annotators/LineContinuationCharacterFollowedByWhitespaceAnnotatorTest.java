package net.sjrx.intellij.plugins.systemdunitfiles.annotators;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest;

import java.util.List;

public class LineContinuationCharacterFollowedByWhitespaceAnnotatorTest extends AbstractUnitFileTest {
  
  public void testThatExcessWhiteSpaceAfterLineContinuationCharacterIsDetectedAsWarningInSimpleCase() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "ExecStart=/bin/bash \\ \n"
                  + "X-Garbage=";
  
    setupFileInEditor("file.service", file);

    // Exercise SUT
    
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
  
    // Verification
    assertSize(1, highlights);
  
    HighlightInfo info = highlights.get(0);
    assertEquals(LineContinuationCharacterFollowedByWhitespaceAnnotator.WARNING_MESSAGE, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);
    
  
    assertEquals(" ", info.getText());
    
  }
  
  public void testThatExcessWhiteSpaceAfterLineContinuationCharacterIsDetectedAsWarningInLongerSimpleCase() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "ExecStart=/bin/bash \\       \n"
                  + "X-Garbage=";
    
    setupFileInEditor("file.service", file);
    
    // Exercise SUT
    
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    
    
    // Verification
    assertSize(1, highlights);
    
    HighlightInfo info = highlights.get(0);
    assertEquals(LineContinuationCharacterFollowedByWhitespaceAnnotator.WARNING_MESSAGE, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);
    
    
    assertEquals("       ", info.getText());
    
  }
  
  public void testThatExcessWhiteSpaceAfterLineContinuationCharacterIsDetectedOnASuccessfulLineContinuation() {
  
    // Fixture Setup
    String file = "[Service]\n"
                  + "ExecStart=/bin/bash \\\n"
                  + "X-Garbage=oeutnhouentuoeh\\ \t \n"
                  + "X-Garbage-Two=";
  
    setupFileInEditor("file.service", file);
  
    // Exercise SUT
  
    List<HighlightInfo> highlights = myFixture.doHighlighting();
  
  
    // Verification
    assertSize(1, highlights);
  
    HighlightInfo info = highlights.get(0);
    assertEquals(LineContinuationCharacterFollowedByWhitespaceAnnotator.WARNING_MESSAGE, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);
  
  
    assertEquals(" \t ", info.getText());
  
  }
  
  public void testThatExcessWhiteSpaceAfterLineContinuationCharacterIsDetectedOnASuccessfulLineContinuationWithComment() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "ExecStart=/bin/bash \\\n"
                  + ";A nice comment\n"
                  + "X-Garbage=\\\t\t\t\n"
                  + "X-Garbage-Two=";
  
    setupFileInEditor("file.service", file);
  
    // Exercise SUT
  
    List<HighlightInfo> highlights = myFixture.doHighlighting();
  
  
    // Verification
    assertSize(1, highlights);
  
    HighlightInfo info = highlights.get(0);
    assertEquals(LineContinuationCharacterFollowedByWhitespaceAnnotator.WARNING_MESSAGE, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);
  
  
    assertEquals("\t\t\t", info.getText());
  }
  
  public void testThatNoExcessWhitespaceAfterLineContinuationCharacterResultsInNoWarnings() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "ExecStart=/bin/bash \\\n"
                  + ";A nice comment\n"
                  + "X-Garbage=\\\n"
                  + "X-Garbage-Two=";
  
    setupFileInEditor("file.service", file);
  
    // Exercise SUT
  
    List<HighlightInfo> highlights = myFixture.doHighlighting();
  
  
    // Verification
    assertSize(0, highlights);
  }
  
  public void testThatNoLineContinuationCharacterResultsInNoWarnings() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "ExecStart=/bin/bash \\\n"
                  + ";A nice comment\n"
                  + "X-Garbage=\t    \\\n"
                  + "X-Garbage-Two=";
  
    setupFileInEditor("file.service", file);
  
    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();
  
    // Verification
    assertSize(0, highlights);
  }
  
}
