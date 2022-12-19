package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFile
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileVisitor
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository

class DeprecatedOptionsInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val file = holder.file
    return if (file !is UnitFile || !file.getLanguage().isKindOf(UnitFileLanguage.INSTANCE)) {
      PsiElementVisitor.EMPTY_VISITOR
    } else MyVisitor(holder)
  }

  private class MyVisitor(private val holder: ProblemsHolder) : UnitFileVisitor() {
    override fun visitPropertyType(property: UnitFilePropertyType) {
      val section = PsiTreeUtil.getParentOfType(property, UnitFileSectionGroups::class.java) ?: return
      val sdr = SemanticDataRepository.getInstance()
      val sectionName = section.sectionName
      val key = property.key
      if (sdr.isDeprecated(sectionName, key)) {
        val text = sdr.getDeprecationReason(sectionName, key, false)
        holder.registerProblem(property.keyNode.psi, text, ProblemHighlightType.LIKE_DEPRECATED)
      }
    }
  }
}
