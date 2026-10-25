package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.codeInspection.LocalInspectionTool
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
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.ValueWarning
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.ValueWarnings
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.fileClass
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.deprecatedTokens

/**
 * Warns about values systemd accepts but complains about (#565): deprecated or obsolete spellings, unsafe settings,
 * and forms systemd silently corrects. Invalid values are the InvalidValue inspection's job; this one only speaks up
 * when the value is otherwise accepted.
 *
 * It reports two sources:
 *  - tokens a grammar marks deprecated ([deprecatedTokens], e.g. `FlexibleLiteralChoiceTerminal.deprecating`);
 *  - whatever a validator implementing [ValueWarnings] returns.
 */
class ValueWarningInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val file = holder.file
    return if (file !is UnitFile || !file.getLanguage().isKindOf(UnitFileLanguage.INSTANCE)) {
      PsiElementVisitor.EMPTY_VISITOR
    } else MyVisitor(holder)
  }

  private class MyVisitor(private val holder: ProblemsHolder) : UnitFileVisitor() {
    override fun visitPropertyType(property: UnitFilePropertyType) {
      val section = PsiTreeUtil.getParentOfType(property, UnitFileSectionGroups::class.java) ?: return
      val value = property.valueText ?: return
      val validator = SemanticDataRepository.instance
        .getOptionValidator(section.containingFile.fileClass(), section.sectionName, property.key)

      val warnings = mutableListOf<ValueWarning>()
      if (validator is GrammarOptionValue) {
        validator.combinator.deprecatedTokens(value).mapTo(warnings) { ValueWarning(it.start, it.end, it.message) }
      }
      if (validator is ValueWarnings) {
        warnings += validator.valueWarnings(property.key, value)
      }

      for (warning in warnings) {
        holder.registerProblem(
          property.valueNode.psi,
          warning.message,
          ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
          TextRange(warning.start, warning.end),
        )
      }
    }
  }
}
