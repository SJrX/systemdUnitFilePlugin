package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFile
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileValueType
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
        validator.combinator.deprecatedTokens(value).mapTo(warnings) { ValueWarning(it.start, it.end, it.message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING) }
      }
      if (validator is ValueWarnings) {
        warnings += validator.valueWarnings(property.key, value)
      }

      if (warnings.isEmpty()) return
      val valueElement = property.valueNode.psi
      val offsets = valueTextOffsets(valueElement, value)
      for (warning in warnings) {
        // The warning's span is in valueText (lines joined, whitespace trimmed); map it back onto the value element.
        val range = if (offsets != null && warning.start < warning.end && warning.end <= value.length) {
          TextRange(offsets[warning.start], offsets[warning.end - 1] + 1)
        } else {
          TextRange(0, valueElement.textLength)
        }
        holder.registerProblem(valueElement, warning.message, warning.highlightType, range)
      }
    }
  }
}

/**
 * For each character of [value] (the element's valueText), its offset inside [valueElement]. Mirrors
 * UnitFileValueType.getValue(): each value line is trimmed (String.trim(), i.e. chars <= ' '), a trailing continuation
 * backslash becomes a space, and the joined result is trimmed again. Null if the reconstruction does not match [value].
 */
private fun valueTextOffsets(valueElement: PsiElement, value: String): IntArray? {
  val text = StringBuilder()
  val offsets = mutableListOf<Int>()
  var child = valueElement.firstChild
  while (child != null) {
    if (UnitFileValueType.valueTypes.contains(child.node.elementType)) {
      val line = child.text
      var from = 0
      var to = line.length
      while (from < to && line[from] <= ' ') from++
      while (to > from && line[to - 1] <= ' ') to--
      for (i in from until to) {
        text.append(if (i == to - 1 && line[i] == '\\') ' ' else line[i])
        offsets += child.startOffsetInParent + i
      }
    }
    child = child.nextSibling
  }
  var from = 0
  var to = text.length
  while (from < to && text[from] <= ' ') from++
  while (to > from && text[to - 1] <= ' ') to--
  if (text.substring(from, to) != value) return null
  return offsets.subList(from, to).toIntArray()
}
