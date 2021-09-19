package net.sjrx.intellij.plugins.systemdunitfiles.documentation;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.documentation.DocumentationProviderEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Collections;
import java.util.List;

public class UnitFileDocumentationProviderTest extends AbstractUnitFileTest {


  private final DocumentationProviderEx sut = new UnitFileDocumentationProvider();

  public void testGetUrlForUnknownKeyReturnsAnEmptyListOfUrls() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "BadKey=Hello Good Sir";

    PsiFile psiFile = setupFileInEditor("file.service", file);


    // Exercise SUT
    PsiElement badKey = getAllKeysInFile(psiFile).get(0);

    List<String> urls = sut.getUrlFor(badKey, badKey);

    // Verification
    assertNull(urls);
  }

  public void testGetUrlForUnknownKeySeparatorReturnsAnEmptyListOfUrls() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "BadKey=Hello Good Sir";

    PsiFile psiFile = setupFileInEditor("file.service", file);


    // Exercise SUT
    PsiElement badKeySeparator = getAllSeparatorsInFile(psiFile).get(0);

    List<String> urls = sut.getUrlFor(badKeySeparator, badKeySeparator);

    // Verification
    assertNull(urls);
  }

  public void testGetUrlForKnownKeyReturnsAValidUrl() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Documentation=http://www.google.com";

    PsiFile psiFile = setupFileInEditor("file.service", file);

    // Exercise SUT
    PsiElement documentationKey = getAllKeysInFile(psiFile).get(0);

    List<String> urls = sut.getUrlFor(documentationKey, documentationKey);

    // Verification
    assertEquals(Collections.singletonList("https://www.freedesktop.org/software/systemd/man/systemd.unit.html#Documentation="), urls);
  }

  public void testGetUrlForKnownKeySeparatorReturnsAValidUrl() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Documentation=http://www.google.com";

    PsiFile psiFile = setupFileInEditor("file.service", file);

    // Exercise SUT
    PsiElement documentationKeySeparator = getAllSeparatorsInFile(psiFile).get(0);

    List<String> urls = sut.getUrlFor(documentationKeySeparator, documentationKeySeparator);

    // Verification
    assertEquals(Collections.singletonList("https://www.freedesktop.org/software/systemd/man/systemd.unit.html#Documentation="), urls);
  }
  
  public void testGetUrlForDeprecatedOptionReturnsAValidUrl() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "BlockIOAccounting=true";
    
    PsiFile psiFile = setupFileInEditor("file.service", file);
    
    // Exercise SUT
    PsiElement documentationKeySeparator = getAllSeparatorsInFile(psiFile).get(0);
  
    List<String> urls = sut.getUrlFor(documentationKeySeparator, documentationKeySeparator);
    
    // Verification
    assertEquals(Collections.singletonList("https://www.freedesktop.org/software/systemd/man/systemd.resource-control.html#BlockIOAccounting="), urls);
  }

  public void testGetUrlForUnknownSectionReturnsAnEmptyListOfUrls() {
    // Fixture Setup
    String file = "[X-Unit]\n"
                  + "Documentation=http://www.google.com";

    PsiFile psiFile = setupFileInEditor("file.service", file);

    // Exercise SUT
    PsiElement unknownSectionHeader = getAllSectionInFile(psiFile).get(0);

    List<String> urls = sut.getUrlFor(unknownSectionHeader, unknownSectionHeader);

    // Verification
    assertNull(urls);
  }

  public void testGetUrlForKnownSectionReturnsAValidUrl() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Documentation=http://www.google.com";

    PsiFile psiFile = setupFileInEditor("file.service", file);

    // Exercise SUT
    PsiElement sectionHeader = getAllSectionInFile(psiFile).get(0);

    List<String> urls = sut.getUrlFor(sectionHeader, sectionHeader);

    // Verification
    assertEquals(Collections.singletonList("https://www.freedesktop.org/software/systemd/man/systemd.unit.html#%5BUnit%5D%20Section%20Options"), urls);
  }


  public void testGenerateDocForUnknownKeyReturnsNull() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "BadKey=Hello Good Sir";

    PsiFile psiFile = setupFileInEditor("file.service", file);


    // Exercise SUT
    PsiElement badKey = getAllKeysInFile(psiFile).get(0);

    String doc = sut.generateDoc(badKey, badKey);

    // Verification
    assertNull(doc);
  }

  public void testGenerateDocForUnknownKeySeparatorReturnsNull() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "BadKey=Hello Good Sir";

    PsiFile psiFile = setupFileInEditor("file.service", file);


    // Exercise SUT
    PsiElement badKeySeparator = getAllSeparatorsInFile(psiFile).get(0);

    String doc = sut.generateDoc(badKeySeparator, badKeySeparator);

    // Verification
    assertNull(doc);
  }

  public void testGenerateDocForKnownKeyReturnsSomeValidText() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Documentation=http://www.google.com";

    PsiFile psiFile = setupFileInEditor("file.service", file);

    // Exercise SUT
    PsiElement documentationKey = getAllKeysInFile(psiFile).get(0);

    String doc = sut.generateDoc(documentationKey, documentationKey);

    // Verification
    assertNotNull(doc);
    assertTrue((doc.length() > 0));
  }

  public void testGenerateDocForKnownKeySeparatorReturnsSomeValidText() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Documentation=http://www.google.com";

    PsiFile psiFile = setupFileInEditor("file.service", file);

    // Exercise SUT
    PsiElement documentationKeySeparator = getAllSeparatorsInFile(psiFile).get(0);

    String doc = sut.generateDoc(documentationKeySeparator, documentationKeySeparator);

    // Verification
    assertNotNull(doc);
    assertTrue((doc.length() > 0));
  }

  public void testGenerateDocForUnknownSectionReturnsNull() {
    // Fixture Setup
    String file = "[X-Unit]\n"
                  + "Documentation=http://www.google.com";

    PsiFile psiFile = setupFileInEditor("file.service", file);

    // Exercise SUT
    PsiElement unknownSectionHeader = getAllSectionInFile(psiFile).get(0);

    String doc = sut.generateDoc(unknownSectionHeader, unknownSectionHeader);

    // Verification
    assertNull(doc);
  }

  public void testGenerateDocForKnownSectionReturnsSomeValidText() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Documentation=http://www.google.com";

    PsiFile psiFile = setupFileInEditor("file.service", file);

    // Exercise SUT
    PsiElement unknownSectionHeader = getAllSectionInFile(psiFile).get(0);

    String doc = sut.generateDoc(unknownSectionHeader, unknownSectionHeader);

    // Verification
    assertNotNull(doc);

    assertTrue(doc.length() > 1);

  }
  
  public void testGenerateDocForCompletedValueOfKnownKeyReturnsSomeValidText() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Documentation=http://www.google.com";
    
    PsiFile psiFile = setupFileInEditor("file.service", file);
    
    // Exercise SUT
    PsiElement documentationValue = getAllCompletedValuesInFile(psiFile).get(0);
    
    documentationValue = sut.getCustomDocumentationElement(myFixture.getEditor(), psiFile, documentationValue, 0);
    
    String doc = sut.generateDoc(documentationValue, documentationValue);
    
    // Verification
    assertNotNull(doc);
    assertTrue((doc.length() > 0));
  }
  
  public void testGenerateDocForCompletedValueOfUnknownSectionReturnsNull() {
    // Fixture Setup
    String file = "[X-Unit]\n"
                  + "Documentation=http://www.google.com";
    
    PsiFile psiFile = setupFileInEditor("file.service", file);
    
    // Exercise SUT
    PsiElement documentationValue = getAllCompletedValuesInFile(psiFile).get(0);
    
    documentationValue = sut.getCustomDocumentationElement(myFixture.getEditor(), psiFile, documentationValue, 0);
    
    String doc = sut.generateDoc(documentationValue, documentationValue);
    
    // Verification
    assertNull(doc);
  }
  
  
  public void testGenerateDocForCompletedValueOfUnknownKeyReturnsNull() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "HelpPage=http://www.google.com";
    
    PsiFile psiFile = setupFileInEditor("file.service", file);
    
    // Exercise SUT
    PsiElement documentationValue = getAllCompletedValuesInFile(psiFile).get(0);
    
    documentationValue = sut.getCustomDocumentationElement(myFixture.getEditor(), psiFile, documentationValue, 0);
    
    String doc = sut.generateDoc(documentationValue, documentationValue);
    
    // Verification
    assertNull(doc);
  }
  
  public void testGenerateDocForContinuingValueOfKnownKeyReturnsSomeValidText() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Documentation=http://www.google.com\\\n"
                  + "hello sir";
    
    PsiFile psiFile = setupFileInEditor("file.service", file);
    
    // Exercise SUT
    PsiElement documentationValue = getAllContinuingValuesInFile(psiFile).get(0);
  
    documentationValue = sut.getCustomDocumentationElement(myFixture.getEditor(), psiFile, documentationValue, 0);
    
    String doc = sut.generateDoc(documentationValue, documentationValue);
    
    // Verification
    assertNotNull(doc);
    assertTrue((doc.length() > 0));
  }
  
  public void testGenerateDocForContinuingValueOfUnknownSectionReturnsNull() {
    // Fixture Setup
    String file = "[X-Unit]\n"
                  + "Documentation=http://www.google.com\\\n"
                  + "hello sir";
    
    PsiFile psiFile = setupFileInEditor("file.service", file);
    
    // Exercise SUT
    PsiElement documentationValue = getAllContinuingValuesInFile(psiFile).get(0);
  
    documentationValue = sut.getCustomDocumentationElement(myFixture.getEditor(), psiFile, documentationValue, 0);
    
    String doc = sut.generateDoc(documentationValue, documentationValue);
    
    // Verification
    assertNull(doc);
  }
  
  
  public void testGenerateDocForContinuingValueOfUnknownKeyReturnsNull() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "HelpPage=http://www.google.com\\\n"
                  + "hello sir";
    
    PsiFile psiFile = setupFileInEditor("file.service", file);
    
    // Exercise SUT
    PsiElement documentationValue = getAllContinuingValuesInFile(psiFile).get(0);
  
    documentationValue = sut.getCustomDocumentationElement(myFixture.getEditor(), psiFile, documentationValue, 0);
    
    String doc = sut.generateDoc(documentationValue, documentationValue);
    
    // Verification
    assertNull(doc);
  }
  
  public void testGenerateDocForContinuingValueAfterCommentsOfKnownKeyReturnsSomeValidText() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "Documentation=http://www.google.com\\\n"
                  + ";Good bye\n"
                  + "#See ya later!\n"
                  + "hello sir";
    
    PsiFile psiFile = setupFileInEditor("file.service", file);
    
    // Exercise SUT
    PsiElement documentationValue = getAllContinuingValuesInFile(psiFile).get(0);
    
    documentationValue = sut.getCustomDocumentationElement(myFixture.getEditor(), psiFile, documentationValue, 0);
    
    String doc = sut.generateDoc(documentationValue, documentationValue);
    
    // Verification
    assertNotNull(doc);
    assertTrue((doc.length() > 0));
  }
  
  public void testGenerateDocForContinuingValueAfterCommentsOfUnknownSectionReturnsNull() {
    // Fixture Setup
    String file = "[X-Unit]\n"
                  + "Documentation=http://www.google.com\\\n"
                  + ";Good bye\n"
                  + "#See ya later!\n"
                  + "hello sir";
    
    PsiFile psiFile = setupFileInEditor("file.service", file);
    
    // Exercise SUT
    PsiElement documentationValue = getAllContinuingValuesInFile(psiFile).get(0);
    
    documentationValue = sut.getCustomDocumentationElement(myFixture.getEditor(), psiFile, documentationValue, 0);
    
    String doc = sut.generateDoc(documentationValue, documentationValue);
    
    // Verification
    assertNull(doc);
  }
  
  
  public void testGenerateDocForContinuingValueAfterCommentsOfUnknownKeyReturnsNull() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "HelpPage=http://www.google.com\\\n"
                  + ";Good bye\n"
                  + "#See ya later!\n"
                  + "hello sir";
    
    PsiFile psiFile = setupFileInEditor("file.service", file);
    
    // Exercise SUT
    PsiElement documentationValue = getAllContinuingValuesInFile(psiFile).get(0);
    
    documentationValue = sut.getCustomDocumentationElement(myFixture.getEditor(), psiFile, documentationValue, 0);
    
    String doc = sut.generateDoc(documentationValue, documentationValue);
    
    // Verification
    assertNull(doc);
  }

  public void testGenerateDocMovedSectionReturnsSomeText() {
    // Fixture Setup
    String file = "[Unit]\n"
                  + "PropagateReloadTo=cpu.mount";
    
    PsiFile psiFile = setupFileInEditor("file.service", file);
    
    // Exercise SUT
    PsiElement renamedSection = getAllKeysInFile(psiFile).get(0);
    
    String doc = sut.generateDoc(renamedSection, renamedSection);
    
    // Verification
    assertNotNull(doc);
    
    assertEquals("Expected the generated documentation to match", "<div class='definition'><pre>PropagateReloadTo</pre>"
                                                                  + "</div><div class='content'><p>The key <var>PropagateReloadTo</var> in"
                                                                  + " section <b>Unit</b> has been renamed to <var>PropagateReloadsTo</var>"
                                                                  + "<p>NOTE: The semantics of the new value may not"
                                                                  + " match the existing value.<p>"
                                                                  + "<a href='https://github.com/systemd/systemd/commit/7f2cddae09'>More"
                                                                  + " information is available here</a></div>", doc);
    
  }
  
  public void testGenerateDocForUnsupportedSectionReturnsSomeValidText() {
    // Fixture Setup
    String file = "[Service]\n"
                  + "PermissionsStartOnly=true";
    
    PsiFile psiFile = setupFileInEditor("file.service", file);
    
    // Exercise SUT
    PsiElement unsupportedSection = getAllKeysInFile(psiFile).get(0);
    
    String doc = sut.generateDoc(unsupportedSection, unsupportedSection);
    
    // Verification
    assertNotNull(doc);
  
    assertEquals("Expected the generated documentation to match", "<div class='definition'><pre>PermissionsStartOnly"
                                                                  + "</pre></div><div class='content'><p><var>PermissionsStartOnly</var> in"
                                                                  + " section <b>Service</b> is not officially supported.<p>"
                                                                  + "<a href='https://github.com/systemd/systemd/blob/v241/NEWS#L561'>"
                                                                  + "More information is available here</a></div>", doc);
  }
  
  public void testGenerateDocFromPsiElementWithNoNodeDoesNotCrash() {
  
    // Fixture Setup
  
    /*
     * The important part of this is that FakePsiElement getNode() returns null.
     */
    PsiElement element = new FakePsiElement() {
      @Override
      public PsiElement getParent() {
        return null;
      }
    };
    
    // Execute SUT
    sut.generateDoc(element, element);
    
    // Verification (Implicit we want to avoid an NPE, so if we don't crash we didn't throw an NPE)
  }
  
  public void testGetUrlFromPsiElementWithNoNodeDoesNotCrash() {
   
    // Fixture Setup
  
    /*
     * The important part of this is that FakePsiElement getNode() returns null.
     */
    PsiElement element = new FakePsiElement() {
      @Override
      public PsiElement getParent() {
        return null;
      }
    };
    
    // Execute SUT
    sut.getUrlFor(element, element);
    
    // Verification (Implicit we want to avoid an NPE, so if we don't crash we didn't throw an NPE).
  }
  
}
