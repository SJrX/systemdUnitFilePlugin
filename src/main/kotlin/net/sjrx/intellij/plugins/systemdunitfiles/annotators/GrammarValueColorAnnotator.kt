package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.coloring.UnitFileHighlighter
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileProperty
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.fileClass
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Role
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.colorize
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings

/**
 * Grammar-based value coloring (#467 / #342), behind the experimental flag.
 *
 * For options validated by the new grammar engine, [colorize] is asked which spans of the value map
 * to which [Role], and each span is painted with the matching text-attributes key. Does nothing when
 * the flag is off, so normal users are unaffected.
 */
class GrammarValueColorAnnotator : Annotator {

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element !is UnitFileProperty) return
    if (!ExperimentalSettings.getInstance(element.project).state.useGrammarParseEngine) return

    val section = PsiTreeUtil.getParentOfType(element, UnitFileSectionType::class.java) ?: return
    val value = element.valueText ?: return
    val base = element.valueNode?.psi?.textRange?.startOffset ?: return
    val fileClass = element.containingFile.fileClass()
    val validator = SemanticDataRepository.instance.getOptionValidator(fileClass, section.sectionName, element.key)
    if (validator !is GrammarOptionValue) return

    for (region in validator.combinator.colorize(value)) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(TextRange(base + region.start, base + region.end))
        .textAttributes(attributesFor(region.role))
        .create()
    }
  }

  companion object {
    fun attributesFor(role: Role): TextAttributesKey = when (role) {
      Role.ENUM -> UnitFileHighlighter.GRAMMAR_ENUM
      Role.LITERAL -> UnitFileHighlighter.GRAMMAR_LITERAL
      Role.OPERATOR -> UnitFileHighlighter.GRAMMAR_OPERATOR
      Role.IDENTIFIER -> UnitFileHighlighter.GRAMMAR_IDENTIFIER
    }
  }
}
