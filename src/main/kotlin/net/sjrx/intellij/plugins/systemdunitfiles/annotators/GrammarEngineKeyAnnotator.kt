package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.JBColor
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileProperty
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.fileClass
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings
import java.awt.Font

/**
 * Debug aid (#467): while the "underline grammar-engine keys" flag is enabled, underline the KEY of
 * every option whose value is validated by a [GrammarOptionValue], so it is obvious at a glance which
 * keys are backed by the new grammar engine versus the original SyntacticMatch/SemanticMatch path.
 *
 * Gated on its own flag ([ExperimentalSettings.State.underlineGrammarEngineKeys]), independent of
 * whether the grammar engine is the active validation path — the grammar validators exist in the
 * registry either way. Does nothing when the flag is off, so it has no effect for normal users.
 */
class GrammarEngineKeyAnnotator : Annotator {

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element !is UnitFileProperty) return
    if (!ExperimentalSettings.getInstance(element.project).state.underlineGrammarEngineKeys) return

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
    // Underline the key, keeping its normal color — recoloring grammar vs non-grammar keys with two
    // different colors was too distracting, especially on light themes.
    val NEW_ENGINE_KEY: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
      "SYSTEMD_UNIT_FILE_NEW_GRAMMAR_ENGINE_KEY",
      TextAttributes(null, null, JBColor.GRAY, EffectType.LINE_UNDERSCORE, Font.PLAIN),
    )
  }
}