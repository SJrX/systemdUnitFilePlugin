package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionType
import java.util.regex.Pattern

class InvalidSectionHeaderNameAnnotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element is UnitFileSectionType) {
      val sectionElement = element
      if (!p.matcher(sectionElement.sectionName).find()) {

        // First child is the section group element
        holder.newAnnotation(HighlightSeverity.ERROR, ANNOTATION_ERROR_MSG).range(element.getFirstChild()).create()
      }
      val text = sectionElement.firstChild.text
      if (text[0] != '[' || text[text.length - 1] != ']') {
        holder.newAnnotation(HighlightSeverity.ERROR, ANNOTATION_ERROR_MSG).range(element.getFirstChild()).create()
      }
    }
  }

  companion object {
    const val ANNOTATION_ERROR_MSG = ("Invalid section. Sections should start with an [, end with an ], and contain only ASCII"
      + " characters except for [ and ] and control characters")

    /**
     * https://specifications.freedesktop.org/desktop-entry-spec/latest/ar01s03.html#group-header
     * Group names may contain all ASCII characters except for [ and ] and control characters.
     * 20-7f are non control characters, and we subtract the [ from that set.
     */
    private val p = Pattern.compile("^[\\x20-\\x7f&&[^\\[]]+$")
  }
}
