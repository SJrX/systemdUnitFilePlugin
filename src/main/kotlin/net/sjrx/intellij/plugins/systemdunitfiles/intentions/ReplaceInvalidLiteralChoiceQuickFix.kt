package net.sjrx.intellij.plugins.systemdunitfiles.intentions

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.psi.impl.UnitFilePropertyImpl

class ReplaceInvalidLiteralChoiceQuickFix(val offset: Int, val invalidToken : String, val replacementToken : String) : LocalQuickFix {

  override fun getName(): String {
    return "Replace '${invalidToken}' with '${replacementToken}'"
  }

  override fun getFamilyName(): String {
    return "Replace invalid value"
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val fullPropertyValue = descriptor.psiElement.text

    val newText = fullPropertyValue.substring(0, offset) + replacementToken + fullPropertyValue.substring(offset+invalidToken.length)
    val newElement = UnitElementFactory.createProperty(project, (descriptor.psiElement.parent as UnitFilePropertyImpl).key, newText)
    val property = PsiTreeUtil.getParentOfType(descriptor.psiElement, UnitFilePropertyImpl::class.java)?: return

    property.replace(newElement)
  }
}
