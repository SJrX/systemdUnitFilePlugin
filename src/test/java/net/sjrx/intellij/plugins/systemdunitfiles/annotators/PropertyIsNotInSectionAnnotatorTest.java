package net.sjrx.intellij.plugins.systemdunitfiles.annotators;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest;

import java.util.List;

public class PropertyIsNotInSectionAnnotatorTest extends AbstractUnitFileTest {

  public void testAnnotated() {
    // Fixture Setup
    String file = "Key=Value\n"
            + "[Service]\n"
            + "Second=Value\n";

    setupFileInEditor("file.service", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertSize(1, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(PropertyIsNotInSectionAnnotator.ANNOTATION_ERROR_MSG, info.getDescription());
    assertEquals(HighlightInfoType.ERROR, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());
    String annotatedText = myFixture.getDocument(myFixture.getFile()).getText(TextRange.create(info.startOffset, info.endOffset));

    assertNotNull(highlightElement);
    assertEquals("Key=Value", annotatedText);
  }

  public void testNotAnnotated() {
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
