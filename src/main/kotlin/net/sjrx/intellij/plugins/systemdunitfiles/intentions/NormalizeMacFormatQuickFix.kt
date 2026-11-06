package net.sjrx.intellij.plugins.systemdunitfiles.intentions

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.psi.impl.UnitFilePropertyImpl

/**
 * Rewrites the MAC address at [offset] (within the option value) from its current spelling [original]
 * to the user's preferred [replacement], e.g. `00-1A-2B-3C-4D-5E` -> `00:1a:2b:3c:4d:5e`.
 */
class NormalizeMacFormatQuickFix(private val offset: Int, private val original: String, private val replacement: String) : LocalQuickFix {

  override fun getName(): String ="Convert MAC address to '$replacement'"

  override fun getFamilyName(): String = "Convert to canonical MAC address format"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val fullPropertyValue = descriptor.psiElement.text
    val newText = fullPropertyValue.substring(0, offset) + replacement + fullPropertyValue.substring(offset + original.length)
    val property = PsiTreeUtil.getParentOfType(descriptor.psiElement, UnitFilePropertyImpl::class.java) ?: return
    val newElement = UnitElementFactory.createProperty(project, property.key, newText)
    property.replace(newElement)
  }
}
