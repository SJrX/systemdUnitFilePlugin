package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileProperty
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.fileClass

class GrammarOptionValueAnnotator : Annotator {

  override fun annotate(property: PsiElement, holder: AnnotationHolder) {
    if (property is UnitFileProperty) {
      val section = PsiTreeUtil.getParentOfType(property, UnitFileSectionGroups::class.java) ?: return
      property.valueText ?: return

      val fileClass = section.containingFile.fileClass()
      val ovi = SemanticDataRepository.instance.getOptionValidator(fileClass, section.sectionName, property.key)

      ovi.highlight(property, holder)



    }
  }

    companion object {
      const val ANNOTATION_ERROR_MSG = "PID files should be avoided in modern projects. Use Type=notify, Type=notify-reload or Type=simple where possible, which does not require use of PID files to determine the main process of a service and avoids needless forking."
    }
}
