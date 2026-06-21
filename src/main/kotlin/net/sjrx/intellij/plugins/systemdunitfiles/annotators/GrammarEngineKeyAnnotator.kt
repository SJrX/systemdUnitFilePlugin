package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileProperty
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.fileClass
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings

/**
 * Debug aid (#467): while the experimental grammar engine is enabled, mark the KEY of every option
 * whose value is validated by that engine (a [GrammarOptionValue]), so it is obvious at a glance
 * which keys exercise the new parse() path versus the original SyntacticMatch/SemanticMatch one.
 *
 * Does nothing when the flag is off, so it has no effect for normal users.
 */
class GrammarEngineKeyAnnotator : Annotator {

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element !is UnitFileProperty) return
    if (!ExperimentalSettings.getInstance(element.project).state.useGrammarParseEngine) return

    val section = PsiTreeUtil.getParentOfType(element, UnitFileSectionType::class.java) ?: return
    val fileClass = element.containingFile.fileClass()
    val validator = SemanticDataRepository.instance.getOptionValidator(fileClass, section.sectionName, element.key)

    if (validator is GrammarOptionValue) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(element.keyNode.psi)
        .textAttributes(NEW_ENGINE_KEY)
        .create()
    }
  }

  companion object {
    // Layered on top of the normal key color; METADATA gives a distinct, theme-aware tint.
    val NEW_ENGINE_KEY: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
      "SYSTEMD_UNIT_FILE_NEW_GRAMMAR_ENGINE_KEY",
      DefaultLanguageHighlighterColors.METADATA,
    )
  }
}
