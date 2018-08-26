package net.sjrx.intellij.plugins.systemdunitfiles.annotators;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.psi.PsiElement;
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest;

import java.util.List;

public class InvalidSectionHeaderNameAnnotatorTest extends AbstractUnitFileTest {

  public void testThatInvalidSectionNamesAreAnnotated() {
    // Fixture Setup
    String file = "[Serv\tice]\n"
                  + "Requires=Hello Good Sir";

    setupFileInEditor("file.service", file);


    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();


    // Verification
    assertSize(1, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(InvalidSectionHeaderNameAnnotator.ANNOTATION_ERROR_MSG, info.getDescription());
    assertEquals(HighlightInfoType.ERROR, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("[Serv\tice]", highlightElement.getText());
  }


  public void testThatInvalidSectionNameWithLeftBraceAreAnnotated() {
    // Fixture Setup
    String file = "[Serv[ice]\n"
                  + "Requires=Hello Good Sir";

    setupFileInEditor("file.service", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertSize(1, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(InvalidSectionHeaderNameAnnotator.ANNOTATION_ERROR_MSG, info.getDescription());
    assertEquals(HighlightInfoType.ERROR, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("[Serv[ice]", highlightElement.getText());
  }

  public void testThatValidSectionNamesAreNotAnnotated() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "Requires=Hello Good Sir";

    setupFileInEditor("file.service", file);


    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();


    // Verification
    assertSize(0, highlights);
  }
}
