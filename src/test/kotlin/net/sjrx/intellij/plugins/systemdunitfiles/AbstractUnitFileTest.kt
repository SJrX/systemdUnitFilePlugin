package net.sjrx.intellij.plugins.systemdunitfiles

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.MatcherAssert.assertThat
import java.util.stream.Collectors

abstract class AbstractUnitFileTest : BasePlatformTestCase() {

  override fun setUp() {
    super.setUp()
    // Pin the grammar-engine flag to false (its historical default) so the shipped production default
    // — flipped to true in #467 — does not leak into tests. This keeps the CI matrix meaningful:
    //   * -Dsystemd.unit.grammarParseEngine=false  -> flag false + FORCE_PARSE_ENGINE false: the OLD
    //     validation engine is exercised (still reachable in production via "Switch back"), so it stays
    //     covered even though it is no longer the default.
    //   * -Dsystemd.unit.grammarParseEngine=true   -> FORCE_PARSE_ENGINE forces the NEW validation
    //     engine regardless of the flag.
    // Tests that need the new engine's flag-gated features (grammar coloring, completion, IPv6
    // inspection) set useGrammarParseEngine = true explicitly, overriding this.
    ExperimentalSettings.getInstance(project).state.useGrammarParseEngine = false
  }

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
      assertTrue("Expected that $value contains $subject", value.contains(subject))
    }

    @JvmStatic
    protected fun assertContainsQuickfix(info: HighlightInfo, quickfixName: String) {
      val quickFixes = info.quickFixActionRanges.map {
        it.first.action.text
      }

      assertThat(quickFixes, hasItem(quickfixName))
    }
  }
}
