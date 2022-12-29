package net.sjrx.intellij.plugins.systemdunitfiles.intentions

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.psi.impl.UnitFilePropertyImpl

class SwapPrefixOrderQuickFix(val offset: Int) : LocalQuickFix {
  override fun getFamilyName(): String {
    return "Swap prefix order"
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val fullPropertyValue = descriptor.psiElement.text
    val newText = fullPropertyValue.substring(0, offset) + "-+" + fullPropertyValue.substring(offset+2)
    val newElement = UnitElementFactory.createProperty(project, (descriptor.psiElement.parent as UnitFilePropertyImpl).key, newText)
    val property = PsiTreeUtil.getParentOfType(descriptor.psiElement, UnitFilePropertyImpl::class.java)?: return

    property.replace(newElement)
  }
}
