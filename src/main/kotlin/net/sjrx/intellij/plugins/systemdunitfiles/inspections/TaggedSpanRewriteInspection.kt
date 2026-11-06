package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFile
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileVisitor
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.fileClass
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SemanticTag
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.labeledRegions
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings

/**
 * Base for inspections that suggest rewriting spans the grammar tagged with [tag] (behind the grammar
 * engine flag): for each such span, if [rewrite] gives a different spelling, report a weak warning with
 * a quick fix. Keying off the tag means only spans the grammar *declared* are touched, never a literal
 * that merely looks similar.
 *
 * Region offsets are in the property's valueText (lines joined and trimmed), so they're mapped back
 * onto the raw value element with [valueTextOffsets] before reporting; values whose mapping fails, or
 * whose mapped text doesn't match, are skipped rather than highlighted in the wrong place.
 */
abstract class TaggedSpanRewriteInspection : LocalInspectionTool() {

  protected abstract val tag: SemanticTag

  /** The rewrite to apply under the current [settings], or null to report nothing at all. */
  protected abstract fun rewriter(settings: ExperimentalSettings.State): ((String) -> String?)?

  protected abstract fun message(replacement: String): String

  protected abstract fun quickFix(original: String, replacement: String): LocalQuickFix

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val file = holder.file
    if (file !is UnitFile || !file.language.isKindOf(UnitFileLanguage.INSTANCE)) return PsiElementVisitor.EMPTY_VISITOR
    val settings = ExperimentalSettings.getInstance(file.project).state
    if (!settings.useGrammarParseEngine) return PsiElementVisitor.EMPTY_VISITOR
    val rewrite = rewriter(settings) ?: return PsiElementVisitor.EMPTY_VISITOR

    return object : UnitFileVisitor() {
      override fun visitPropertyType(property: UnitFilePropertyType) {
        val section = PsiTreeUtil.getParentOfType(property, UnitFileSectionGroups::class.java) ?: return
        val value = property.valueText ?: return
        val validator = SemanticDataRepository.instance
          .getOptionValidator(section.containingFile.fileClass(), section.sectionName, property.key)
        if (validator !is GrammarOptionValue) return

        val regions = validator.combinator.labeledRegions(value).filter { it.tag == tag && it.start < it.end }
        if (regions.isEmpty()) return
        val valueElement = property.valueNode.psi
        val rawText = valueElement.text
        val offsets = valueTextOffsets(valueElement, value) ?: return

        for (region in regions) {
          val text = value.substring(region.start, region.end)
          val replacement = rewrite(text) ?: continue
          if (replacement == text) continue
          val range = TextRange(offsets[region.start], offsets[region.end - 1] + 1)
          if (range.substring(rawText) != text) continue
          holder.registerProblem(valueElement, message(replacement), ProblemHighlightType.WEAK_WARNING, range, quickFix(text, replacement))
        }
      }
    }
  }
}
