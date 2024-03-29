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

/**
 * The purpose of this inspection is to catch any warnings that may be generated by systemd when processing a unit file.
 *
 *
 * The rules state:
 *
 * <pre>
 * Unit files may contain additional options on top of those listed here. If systemd encounters an unknown option,
 * it will write a warning log message but continue loading the unit. If an option or section name is prefixed with X-,
 * it is ignored completely by systemd. Options within an ignored section do not need the prefix.
 * Applications may use this to include additional information in the unit files.
</pre> *  (Source: https://www.freedesktop.org/software/systemd/man/systemd.unit.html#)
 */
class UnknownKeyInSectionInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val file = holder.file
    return if (file !is UnitFile || !file.getLanguage().isKindOf(UnitFileLanguage.INSTANCE)) {
      PsiElementVisitor.EMPTY_VISITOR
    } else MyVisitor(holder)
  }

  private class MyVisitor(private val holder: ProblemsHolder) : UnitFileVisitor() {
    override fun visitPropertyType(property: UnitFilePropertyType) {
      val key = property.key
      if (key.startsWith(IGNORED_SECTION_OR_KEY_PREFIX)) {
        return
      }
      val section = PsiTreeUtil.getParentOfType(property, UnitFileSectionGroups::class.java) ?: return
      val sectionName = section.sectionName
      if (sectionName.startsWith(IGNORED_SECTION_OR_KEY_PREFIX)) {
        return
      }

      if (!SemanticDataRepository.instance.getAllowedKeywordsInSectionFromValidators(sectionName).contains(key)) {
        // TODO Figure out what highlight to use
        holder.registerProblem(property.keyNode.psi, INSPECTION_TOOL_TIP_TEXT, ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
      }
    }
  }

  companion object {
    private const val IGNORED_SECTION_OR_KEY_PREFIX = "X-"
    const val INSPECTION_TOOL_TIP_TEXT = "This key is unrecognized which will cause systemd to generate a warning when loading this unit."
  }
}
