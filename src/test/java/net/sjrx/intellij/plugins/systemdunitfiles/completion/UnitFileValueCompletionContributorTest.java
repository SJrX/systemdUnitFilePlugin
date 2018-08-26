package net.sjrx.intellij.plugins.systemdunitfiles.completion;

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest;

import java.util.List;

public class UnitFileValueCompletionContributorTest extends AbstractUnitFileTest {
  
  public void testCompletionOfUnknownKeyInKnownSectionIsEmpty() {
    // Fixture Setup
    String file = "[Install]\n"
                  + "Whatevs=" + COMPLETION_POSITION + "\n";
  
  
    myFixture.configureByText("file.service", file);
  
    List<String> completions = getBasicCompletionResultStrings();
  
    assertSize(0, completions);
  }
  
  public void testCompletionOfUnknownKeyInUnKnownSectionIsEmpty() {
    // Fixture Setup
    String file = "[Tester]\n"
                  + "Whatevs=" + COMPLETION_POSITION + "\n";
  
  
    myFixture.configureByText("file.service", file);
  
    // Execute SUT
    List<String> completions = getBasicCompletionResultStrings();
  
    // Verification
    assertSize(0, completions);
  }
  
  public void testCompletionOfBooleanOptionReturnsValues() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "TTYReset=" + COMPLETION_POSITION + "\n";
  
  
    myFixture.configureByText("file.service", file);
  
    // Execute SUT
    List<String> completions = getBasicCompletionResultStrings();
  
    // Verification
    assertContainsElements(completions, "on","off","true", "false", "yes", "no");
  }
  
  public void testCompletionOfKillModeOptionReturnsValues() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "KillMode=o" + COMPLETION_POSITION + "\n";
  
  
    myFixture.configureByText("file.service", file);
  
    // Execute SUT
    List<String> completions = getBasicCompletionResultStrings();
  
    // Verification
    assertContainsElements(completions, "control-group", "process", "none");
  }
  
  public void testCompletionOfRestartOptionReturnsValues() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "Restart=or" + COMPLETION_POSITION + "\n";
  
  
    myFixture.configureByText("file.service", file);
  
    // Execute SUT
    List<String> completions = getBasicCompletionResultStrings();
  
    // Verification
    assertContainsElements(completions, "on-abnormal", "on-abort");
  }
  
  public void testCompletionOfServiceTypeOptionReturnsValues() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "Type=o" + COMPLETION_POSITION + "\n";
  
  
    myFixture.configureByText("file.service", file);
  
    // Execute SUT
    List<String> completions = getBasicCompletionResultStrings();
  
    // Verification
    assertContainsElements(completions, "forking", "oneshot", "notify");
  }
  
}
