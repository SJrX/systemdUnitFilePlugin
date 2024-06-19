package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileProperty
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionType

class PidFileOptionWarning : Annotator {

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element is UnitFileProperty) {
      val section = PsiTreeUtil.getParentOfType(element, UnitFileSectionType::class.java)
      if (element.key == "PIDFile" && section?.sectionName == "Service") {
        holder.newAnnotation(HighlightSeverity.WEAK_WARNING, ANNOTATION_ERROR_MSG).range(element.keyNode.psi).create()
      }
    }
  }

    companion object {
      const val ANNOTATION_ERROR_MSG = "PID files should be avoided in modern projects. Use Type=notify, Type=notify-reload or Type=simple where possible, which does not require use of PID files to determine the main process of a service and avoids needless forking."
    }
}
