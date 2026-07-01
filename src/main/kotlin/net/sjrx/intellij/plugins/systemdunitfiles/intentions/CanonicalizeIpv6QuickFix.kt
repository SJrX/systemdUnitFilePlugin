package net.sjrx.intellij.plugins.systemdunitfiles.intentions

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.psi.impl.UnitFilePropertyImpl

/**
 * Replaces the IPv6 address at [offset] (within the option value) with its RFC 5952 [canonical] form.
 */
class CanonicalizeIpv6QuickFix(private val offset: Int, private val original: String, private val canonical: String) : LocalQuickFix {

  override fun getName(): String = "Convert to canonical IPv6 '$canonical'"

  override fun getFamilyName(): String = "Convert to canonical IPv6 (RFC 5952)"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val fullPropertyValue = descriptor.psiElement.text
    val newText = fullPropertyValue.substring(0, offset) + canonical + fullPropertyValue.substring(offset + original.length)
    val property = PsiTreeUtil.getParentOfType(descriptor.psiElement, UnitFilePropertyImpl::class.java) ?: return
    val newElement = UnitElementFactory.createProperty(project, property.key, newText)
    property.replace(newElement)
  }
}
