package net.sjrx.intellij.plugins.systemdunitfiles.inspections;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.psi.PsiElement;

import java.util.List;

@SuppressWarnings("ConstantConditions")
public class UnknownKeyInSectionInspectionTest extends AbstractInspectionTest {

  public void testValidFileHasNoErrors() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Description=Hello Good Sir\n"
                  + "[Install]\n"
                  + "Alias=Foo\n"
                  + "[Service]\n"
                  + "SuccessExitStatus=5";

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

  public void testKeyFromInstallSectionThrowsWarningInUnitSection() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Alias=Hello Good Sir";

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

    assertEquals("Alias", highlightElement.getText());
  }

  public void testKeyFromUnitSectionThrowsWarningInInstallSection() {
    // Fixture Setup
    String file = "[Install]\n"
                  + "Requires=Hello Good Sir";

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

    assertEquals("Requires", highlightElement.getText());
  }

  public void testKeyFromInstallSectionThrowsWarningInServiceSection() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "Alias=Hello Good Sir";

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

    assertEquals("Alias", highlightElement.getText());
  }

  public void testKeyFromUnitSectionThrowsWarningInServiceSection() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "Requires=Hello Good Sir";

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

    assertEquals("Requires", highlightElement.getText());
  }

  public void testKeyFromResourceControlManPageDoesNotThrowWarningInServiceSection() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "CPUAccounting=on";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("file.service", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertEmpty(highlights);
  }

  public void testKeyFromExecManPageDoesNotThrowWarningInServiceSection() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "DynamicUser=yes";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("file.service", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertEmpty(highlights);
  }

  public void testAutomountFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    String file = "[Automount]\n"
                  + "Where=yes";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.automount", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertEmpty(highlights);
  }

  public void testAutomountFileTypeThrowsWarningWithKeyFromServiceFile() {
    // Fixture Setup
    String file = "[Automount]\n"
                  + "BusName=yes";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.automount", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertSize(1, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("BusName", highlightElement.getText());
  }

  public void testDeviceFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Description=SomeUnit";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.device", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertEmpty(highlights);
  }

  public void testDeviceFileTypeThrowsWarningWithKeyFromServiceFile() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "BusName=yes";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.device", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertSize(1, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("BusName", highlightElement.getText());
  }

  public void testMountFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    String file = "[Mount]\n"
                  + "SloppyOptions=true";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.mount", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertEmpty(highlights);
  }

  public void testMountFileTypeThrowsWarningWithKeyFromServiceFile() {
    // Fixture Setup
    String file = "[Mount]\n"
                  + "BusName=yes";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.mount", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertSize(1, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("BusName", highlightElement.getText());
  }

  public void testPathFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    String file = "[Path]\n"
                  + "MakeDirectory=true";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.path", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertEmpty(highlights);
  }

  public void testPathFileTypeThrowsWarningWithKeyFromServiceFile() {
    // Fixture Setup
    String file = "[Path]\n"
                  + "BusName=yes";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.path", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertSize(1, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("BusName", highlightElement.getText());
  }

  public void testServiceFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "BusName=true";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.service", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertEmpty(highlights);
  }

  public void testServiceFileTypeThrowsWarningWithKeyFromPathFile() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "MakeDirectory=yes";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.service", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertSize(1, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("MakeDirectory", highlightElement.getText());
  }

  public void testSliceFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    String file = "[Slice]\n"
                  + "CPUAccounting=true";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.slice", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertEmpty(highlights);
  }

  public void testSliceFileTypeThrowsWarningWithKeyFromUnitSection() {
    // Fixture Setup
    String file = "[Slice]\n"
                  + "Description=yes";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.slice", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertSize(1, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("Description", highlightElement.getText());
  }

  public void testSocketFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    String file = "[Socket]\n"
                  + "Backlog=5";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.socket", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertEmpty(highlights);
  }

  public void testSocketFileTypeThrowsWarningWithKeyFromServiceFile() {
    // Fixture Setup
    String file = "[Socket]\n"
                  + "BusName=yes";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.socket", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertSize(1, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("BusName", highlightElement.getText());
  }

  public void testSwapFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    String file = "[Swap]\n"
                  + "Priority=5";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.swap", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertEmpty(highlights);
  }

  public void testSwapFileTypeThrowsWarningWithKeyFromServiceFile() {
    // Fixture Setup
    String file = "[Swap]\n"
                  + "BusName=yes";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.swap", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertSize(1, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("BusName", highlightElement.getText());
  }

  public void testTargetFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Description=SomeUnit";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.target", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertEmpty(highlights);
  }

  public void testTargetFileTypeThrowsWarningWithKeyFromServiceFile() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "BusName=yes";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.target", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertSize(1, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("BusName", highlightElement.getText());
  }

  public void testTimerFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    String file = "[Timer]\n"
                  + "RandomizedDelaySec=50";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.timer", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertEmpty(highlights);
  }

  public void testTimerFileTypeThrowsWarningWithKeyFromServiceFile() {
    // Fixture Setup
    String file = "[Timer]\n"
                  + "BusName=yes";

    enableInspection(UnknownKeyInSectionInspection.class);
    setupFileInEditor("some.timer", file);

    // Exercise SUT
    List<HighlightInfo> highlights = myFixture.doHighlighting();

    // Verification
    assertSize(1, highlights);

    HighlightInfo info = highlights.get(0);
    assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info.getDescription());
    assertEquals(HighlightInfoType.WARNING, info.type);

    PsiElement highlightElement = myFixture.getFile().findElementAt(info.getStartOffset());

    assertEquals("BusName", highlightElement.getText());
  }
}
