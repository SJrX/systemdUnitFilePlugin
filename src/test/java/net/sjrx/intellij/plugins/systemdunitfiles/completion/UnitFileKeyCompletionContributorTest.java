package net.sjrx.intellij.plugins.systemdunitfiles.completion;

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest;

import java.util.List;


public class UnitFileKeyCompletionContributorTest extends AbstractUnitFileTest {
  

  public void testCompletionInInstallSectionReturnsExpectedValues() {
    // Fixture Setup
    String file = "[Install]\n"
                  + "Al" + COMPLETION_POSITION + "\n"
                  + "DefaultInstance=thueo";
    
    
    myFixture.configureByText("file.service", file);
    
    // Exercise SUT
    List<String> completions = getBasicCompletionResultStrings();
  
    // Verification
    assertContainsElements(completions, "Alias=", "Also=");
  }
  
  public void testCompletionInInstallSectionReturnsExpectedValuesWhenAtEndOfFile() {
    // Fixture Setup
    String file = "[Install]\n"
                  + "Al" + COMPLETION_POSITION;
    
    myFixture.configureByText("file.service", file);
  
    // Exercise SUT
    List<String> completions = getBasicCompletionResultStrings();
  
    // Verification
    assertContainsElements(completions, "Alias=", "Also=");
    assertSize(2, completions);
  }
  
  public void testCompletionOfImpossibleToMatchKeyReturnsEmpty() {
    // Fixture Setup
    String file = "[Install]\n"
                  + "ZzzZZZZ" + COMPLETION_POSITION;
    
    myFixture.configureByText("file.service", file);
  
    // Exercise SUT
    List<String> completions = getBasicCompletionResultStrings();
  
    // Verification
    assertEmpty(completions);
  }
  
  public void testCompletionInUnknownSectionReturnsEmpty() {
    // Fixture Setup
    String file = "[X-Unknown]\n"
                  + "Al" + COMPLETION_POSITION;
    
    myFixture.configureByText("file.service", file);
  
    // Exercise SUT
    List<String> completions = getBasicCompletionResultStrings();
  
    // Verification
    assertEmpty(completions);
  }
  
  public void testCompletionInPathSectionReturnsExpectedValuesWhenAtEndOfFile() {
    // Fixture Setup
    String file = "[Path]\n"
                  + "Pat" + COMPLETION_POSITION;
    
    myFixture.configureByText("file.service", file);
  
    // Exercise SUT
    List<String> completions = getBasicCompletionResultStrings();
  
    // Verification
    assertContainsElements(completions, "PathExists=", "PathExistsGlob=", "PathChanged=", "PathModified=");
    assertSize(4, completions);
  }
  
  public void testTheFollowingDoesNotThrowANullPointerException() {
    // Fixture Setup
    String file = "[Swap]\n"
                  + "M\n"
                  + "\n"
                  + "[Path]\n"
                  + "MakeDirectory=\n"
                  + "M" + COMPLETION_POSITION + "\n"
                  + "\n";
    
    myFixture.configureByText("file.service", file);
    
    // Exercise SUT
    List<String> completions = getBasicCompletionResultStrings();
    
    // Verification
    assertEmpty(completions);
  }
}