package net.sjrx.intellij.plugins.systemdunitfiles.inspections;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.psi.PsiElement;

import java.util.List;

public class UnknownKeyInSectionInspectionTest extends AbstractInspectionTest {

  public void testValidFileHasNoErrors() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Description=Hello Good Sir";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("file.service", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertEmpty(highlights);
  }

  public void testUnknownKeyInUnitSectionGeneratesWarning() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "BadKey=Hello Good Sir";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("file.service", file);


    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();


    // Verification
    assertSize(1, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("BadKey", highlightElement.getText());
  }

  public void testUnknownKeyInInstallSectionGeneratesWarning() {
    // Fixture Setup
    String file = "[Install]\n"
                  + "BadKey=Hello Good Sir";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("file.service", file);


    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();


    // Verification
    assertSize(1, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("BadKey", highlightElement.getText());
  }

  public void testUnknownKeyInServiceSectionGeneratesWarning() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "BadKey=Hello Good Sir";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("file.service", file);


    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();


    // Verification
    assertSize(1, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("BadKey", highlightElement.getText());
  }

  public void testTwoUnknownKeysInSameSectionReturnError() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "BadKey=Hello Good Sir\n"
                  + "BadKeyThree=Hello Good Sir";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("file.service", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();


    // Verification
    assertSize(2, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("BadKey", highlightElement.getText());

    info = highlights.get(1);
    assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);

    highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("BadKeyThree", highlightElement.getText());
  }

  public void testTwoUnknownKeysInDistinctSectionsReturnErrors() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "BadKey=Hello Good Sir\n"
                  + "[Unit]\n"
                  + "BadKeyThree=Hello Good Sir";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("file.service", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();


    // Verification
    assertSize(2, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("BadKey", highlightElement.getText());

    info = highlights.get(1);
    assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);

    highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("BadKeyThree", highlightElement.getText());
  }


  public void testKeyStartingWithXDashDoesNotReturnError() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "X-BadKey=Hello Good Sir";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("file.service", file);


    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();


    // Verification
    assertSize(0, highlights);
  }

  public void testSectionStartingWithXDashAndBadKeysDoNotCauseError() {
    // Fixture Setup
    String file = "[X-Service]\n"
                  + "BadKey=Hello Good Sir";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("file.service", file);


    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();


    // Verification
    assertSize(0, highlights);
  }
}
