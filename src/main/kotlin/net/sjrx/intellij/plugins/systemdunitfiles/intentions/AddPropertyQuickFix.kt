package net.sjrx.intellij.plugins.systemdunitfiles.intentions

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.endOffset
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups

class AddPropertyQuickFix(val section: String, val key: String) : LocalQuickFix {
  override fun getFamilyName(): String {
    return "Add ${key} to ${section}"
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {

    descriptor.psiElement.containingFile ?: return
    val sectionGroup = PsiTreeUtil.getParentOfType(descriptor.psiElement, UnitFileSectionGroups::class.java) ?: return
    val dummyFile = UnitElementFactory.createFile(project, sectionGroup.text + "\n${key}=")

    val newElement = sectionGroup.replace(dummyFile.firstChild)

    (dummyFile.lastChild.navigationElement as? Navigatable)?.navigate(true)

    FileEditorManager.getInstance(project).selectedTextEditor?.caretModel?.moveToOffset(newElement.endOffset)
  }

}

