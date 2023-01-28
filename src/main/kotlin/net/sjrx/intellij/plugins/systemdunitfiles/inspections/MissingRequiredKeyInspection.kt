package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import net.sjrx.intellij.plugins.systemdunitfiles.intentions.AddPropertyQuickFix
import net.sjrx.intellij.plugins.systemdunitfiles.psi.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import java.util.*

class MissingRequiredKeyInspection : LocalInspectionTool() {
  override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
    super.checkFile(file, manager, isOnTheFly)

    val requiredKeys = SemanticDataRepository.instance.getRequiredKeys(file.containingFile.name)
    val currentKeys = getAllCanonicalizedNames(file)

    var foundRequiredKey = false
    requiredKeys.forEach {
      foundRequiredKey = foundRequiredKey || it in currentKeys
    }

    val sections = file.childrenOfType<UnitFileSectionGroups>()

    val holder = ProblemsHolder(manager, file, isOnTheFly)

    if (!foundRequiredKey) {
      sections.forEach { section ->
        var missingRequiredKeyMatchesSection = false
        val quickFixes : MutableList<AddPropertyQuickFix> = ArrayList()
        requiredKeys.forEach { key ->
          if(key.startsWith(section.sectionName)) {
            quickFixes.add(AddPropertyQuickFix(section.sectionName, key.split(".")[1]))
            missingRequiredKeyMatchesSection = true
          }
        }

        if (missingRequiredKeyMatchesSection) {
          holder.registerProblem(section.firstChild,
                                 "At least one of the following following keys are required for this type of Unit: ${requiredKeys}",
                                 ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                 *quickFixes.toTypedArray())
        }
      }

    }

    return holder.resultsArray
  }

  fun getAllCanonicalizedNames(file: PsiFile): Set<String> {
    val visitor = MyVisitor()
    file.accept(visitor)

    return visitor.keys
  }



  private class MyVisitor() : UnitFileVisitor() {
    val keys = TreeSet<String>()

    override fun visitElement(element: PsiElement) {
      super.visitElement(element)
      element.acceptChildren(this)
    }
    override fun visitPropertyType(property: UnitFilePropertyType) {
      val section = PsiTreeUtil.getParentOfType(property, UnitFileSectionGroups::class.java) ?: return
      val key = property.key
      keys.add("${section.sectionName}.${key}")
    }
  }

}

