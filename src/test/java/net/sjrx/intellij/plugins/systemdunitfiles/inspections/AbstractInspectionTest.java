package net.sjrx.intellij.plugins.systemdunitfiles.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractInspectionTest extends LightPlatformCodeInsightFixtureTestCase {

  @SuppressWarnings("unchecked")
  protected void enableInspection(Class<? extends LocalInspectionTool> cls) {
    myFixture.enableInspections(cls);
  }

  protected PsiFile setupFileInEditor(String fileName, String contents) {
    return myFixture.configureByText(fileName, contents);
  }


  protected List<PsiElement> getAllSectionInFile(PsiFile psiFile) {
    return getAllElementsByElementType(psiFile, UnitFileElementTypeHolder.SECTION);
  }

  protected List<PsiElement> getAllKeysInFile(PsiFile psiFile) {
    return getAllElementsByElementType(psiFile, UnitFileElementTypeHolder.KEY);
  }

  protected List<PsiElement> getAllSeparatorsInFile(PsiFile psiFile) {
    return getAllElementsByElementType(psiFile, UnitFileElementTypeHolder.SEPARATOR);
  }

  private List<PsiElement> getAllElementsByElementType(PsiFile psiFile, IElementType iet) {
    return PsiTreeUtil.findChildrenOfType(psiFile, PsiElement.class).stream().filter(e -> e.getNode().getElementType().equals(
      iet)).collect(Collectors.toList());
  }
}
