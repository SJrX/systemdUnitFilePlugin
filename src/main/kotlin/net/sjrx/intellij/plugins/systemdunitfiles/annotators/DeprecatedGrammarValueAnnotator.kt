package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileProperty
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.fileClass
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.deprecatedTokens
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings

/**
 * Flags valid-but-deprecated values with a weak warning (#467), behind the experimental flag.
 *
 * Generic: any grammar can mark choices deprecated (see [GrammarOptionValue]'s terminals). The first
 * user is RestrictAddressFamilies=, warning on kernel-removed families like AF_DECnet.
 */
class DeprecatedGrammarValueAnnotator : Annotator {

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element !is UnitFileProperty) return
    if (!ExperimentalSettings.getInstance(element.project).state.useGrammarParseEngine) return

    val section = PsiTreeUtil.getParentOfType(element, UnitFileSectionType::class.java) ?: return
    val value = element.valueText ?: return
    val base = element.valueNode?.psi?.textRange?.startOffset ?: return
    val fileClass = element.containingFile.fileClass()
    val validator = SemanticDataRepository.instance.getOptionValidator(fileClass, section.sectionName, element.key)
    if (validator !is GrammarOptionValue) return

    for (deprecated in validator.combinator.deprecatedTokens(value)) {
      holder.newAnnotation(HighlightSeverity.WEAK_WARNING, deprecated.message)
        .range(TextRange(base + deprecated.start, base + deprecated.end))
        .create()
    }
  }
}
