package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileProperty
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionType

class PropertyIsNotInSectionAnnotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element is UnitFileProperty) {
      val section = PsiTreeUtil.getParentOfType(element, UnitFileSectionType::class.java)
      if (section == null) {
        holder.newAnnotation(HighlightSeverity.ERROR, ANNOTATION_ERROR_MSG).range(element).create()
      }
    }
  }

  companion object {
    const val ANNOTATION_ERROR_MSG = "Property must be under section"
  }
}
