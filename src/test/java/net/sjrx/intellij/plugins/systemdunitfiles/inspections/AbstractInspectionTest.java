package net.sjrx.intellij.plugins.systemdunitfiles.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

public abstract class AbstractInspectionTest extends LightPlatformCodeInsightFixtureTestCase {

  @SuppressWarnings("unchecked")
  protected void enableInspection(Class<? extends LocalInspectionTool> cls) {
    myFixture.enableInspections(cls);
  }

  protected void setupFileInEditor(String fileName, String contents) {
    myFixture.configureByText(fileName, contents);
  }
}
