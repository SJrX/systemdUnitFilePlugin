package net.sjrx.intellij.plugins.systemdunitfiles.intentions

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager

/**
 * Replaces one span of an option value, [original], with [replacement], editing the document in place
 * so the rest of the value (other entries, line continuations, indentation) is left untouched.
 *
 * The span is the descriptor's range within its element. A value can carry several of these fixes (e.g.
 * a list of MAC addresses), and applying one shifts the text after it, so the recorded range can be
 * stale by the time a later fix runs ("Fix all"). The fix therefore only edits when the range still
 * holds [original]; failing that, it falls back to a single whole-token occurrence of [original]. If
 * neither holds it does nothing rather than corrupt the value.
 */
open class ReplaceValueSpanQuickFix(
  private val original: String,
  private val replacement: String,
  private val name: String,
  private val family: String,
) : LocalQuickFix {

  override fun getName(): String = name

  override fun getFamilyName(): String = family

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement ?: return
    if (!element.isValid) return
    val start = locate(element.text, descriptor.textRangeInElement) ?: return
    val documentManager = PsiDocumentManager.getInstance(project)
    val document = documentManager.getDocument(element.containingFile) ?: return
    val absoluteStart = element.textRange.startOffset + start
    document.replaceString(absoluteStart, absoluteStart + original.length, replacement)
    documentManager.commitDocument(document)
  }

  private fun locate(text: String, range: TextRange?): Int? {
    if (range != null && range.endOffset <= text.length && range.substring(text) == original) {
      return range.startOffset
    }
    val matches = generateSequence(text.indexOf(original).takeIf { it >= 0 }) { previous ->
      text.indexOf(original, previous + 1).takeIf { it >= 0 }
    }.filter { isWholeToken(text, it) }.take(2).toList()
    return matches.singleOrNull()
  }

  private fun isWholeToken(text: String, start: Int): Boolean {
    val end = start + original.length
    return (start == 0 || text[start - 1].isWhitespace()) && (end == text.length || text[end].isWhitespace())
  }
}
