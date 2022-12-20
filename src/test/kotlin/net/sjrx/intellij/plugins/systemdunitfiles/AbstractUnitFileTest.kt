package net.sjrx.intellij.plugins.systemdunitfiles

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder
import java.util.*
import java.util.stream.Collectors

abstract class AbstractUnitFileTest : BasePlatformTestCase() {
  protected fun enableInspection(cls: Class<out LocalInspectionTool?>?) {
    myFixture.enableInspections(cls)
  }

  protected fun setupFileInEditor(fileName: String, contents: String): PsiFile {
    return myFixture.configureByText(fileName, contents)
  }

  protected fun getAllSectionInFile(psiFile: PsiFile): List<PsiElement> {
    return getAllElementsByElementType(psiFile, UnitFileElementTypeHolder.SECTION)
  }

  protected fun getAllKeysInFile(psiFile: PsiFile): List<PsiElement> {
    return getAllElementsByElementType(psiFile, UnitFileElementTypeHolder.KEY)
  }

  protected fun getAllSeparatorsInFile(psiFile: PsiFile): List<PsiElement> {
    return getAllElementsByElementType(psiFile, UnitFileElementTypeHolder.SEPARATOR)
  }

  protected fun getAllContinuingValuesInFile(psiFile: PsiFile): List<PsiElement> {
    return getAllElementsByElementType(psiFile, UnitFileElementTypeHolder.CONTINUING_VALUE)
  }

  protected fun getAllCompletedValuesInFile(psiFile: PsiFile): List<PsiElement> {
    return getAllElementsByElementType(psiFile, UnitFileElementTypeHolder.COMPLETED_VALUE)
  }

  private fun getAllElementsByElementType(psiFile: PsiFile, iet: IElementType): List<PsiElement> {
    return PsiTreeUtil.findChildrenOfType(psiFile, PsiElement::class.java).stream().filter { e: PsiElement ->
      e.node.elementType ==
        iet
    }.collect(Collectors.toList())
  }

  protected val basicCompletionResultStrings: List<String>
    get() = myFixture.complete(CompletionType.BASIC).map { it.lookupString }


  companion object {

    const val COMPLETION_POSITION = "<caret>"

    @JvmStatic
    protected fun assertStringContains(subject: String, value: String) {
      TestCase.assertTrue("Expected that $value contains $subject", value.contains(subject))
    }
  }
}
