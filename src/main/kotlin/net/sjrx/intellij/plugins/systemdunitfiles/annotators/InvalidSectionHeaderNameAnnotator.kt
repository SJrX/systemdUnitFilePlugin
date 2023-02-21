package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import java.util.regex.Pattern

class InvalidSectionHeaderNameAnnotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element is UnitFileSectionType) {

      var validSection = true
      if (!p.matcher(element.sectionName).find()) {

        // First child is the section group element
        holder.newAnnotation(HighlightSeverity.ERROR, ILLEGAL_SECTION_NAME).range(element.getFirstChild()).create()
        validSection = false

      }
      val text = element.firstChild.text
      if (text[0] != '[' || text[text.length - 1] != ']') {
        holder.newAnnotation(HighlightSeverity.ERROR, ILLEGAL_SECTION_NAME).range(element.getFirstChild()).create()
        validSection = false
      }


      if (validSection) {
        val sectionName = text.substring(1, text.length - 1)
        val allowedSections = SemanticDataRepository.instance.getAllowedSectionsInFile(element.containingFile.name)
        val unitType = SemanticDataRepository.instance.getUnitType(element.containingFile.name)

        // Sections that start with an X- are ignored by systemd
        // https://www.freedesktop.org/software/systemd/man/systemd.unit.html#Description
        if ((sectionName !in allowedSections) && !sectionName.startsWith("X-")){
          val errorString = SECTION_IN_WRONG_FILE.format(sectionName, unitType, allowedSections)
          holder.newAnnotation(HighlightSeverity.ERROR, errorString).range(element.getFirstChild()).create()
        }
      }


    }
  }

  companion object {
    const val ILLEGAL_SECTION_NAME =
      "Invalid section. Sections should start with an [, end with an ], and contain only ASCII characters except for [ and ] and control characters"

    const val SECTION_IN_WRONG_FILE =
      "The section %s is not allowed in %s files, only the following are allowed: %s"
    /**
     * https://specifications.freedesktop.org/desktop-entry-spec/latest/ar01s03.html#group-header
     * Group names may contain all ASCII characters except for [ and ] and control characters.
     * 20-7f are non control characters, and we subtract the [ from that set.
     */
    private val p = Pattern.compile("^[\\x20-\\x7f&&[^\\[]]+$")
  }
}
