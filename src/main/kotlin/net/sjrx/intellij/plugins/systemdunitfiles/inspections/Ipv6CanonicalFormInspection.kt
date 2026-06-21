package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import net.sjrx.intellij.plugins.systemdunitfiles.intentions.CanonicalizeIpv6QuickFix
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFile
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileVisitor
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.fileClass
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.canonicalizeIpv6
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.labeledRegions
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings

/**
 * Suggests rewriting an IPv6 address to its RFC 5952 canonical form (#363), behind the experimental
 * flag. It walks the grammar's labeled value spans (e.g. a whole IP address) and offers a quick-fix
 * for any that aren't already canonical.
 */
class Ipv6CanonicalFormInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val file = holder.file
    if (file !is UnitFile || !file.language.isKindOf(UnitFileLanguage.INSTANCE)) return PsiElementVisitor.EMPTY_VISITOR
    if (!ExperimentalSettings.getInstance(file.project).state.useGrammarParseEngine) return PsiElementVisitor.EMPTY_VISITOR
    return MyVisitor(holder)
  }

  private class MyVisitor(private val holder: ProblemsHolder) : UnitFileVisitor() {
    override fun visitPropertyType(property: UnitFilePropertyType) {
      val section = PsiTreeUtil.getParentOfType(property, UnitFileSectionGroups::class.java) ?: return
      val value = property.valueText ?: return
      val validator = SemanticDataRepository.instance
        .getOptionValidator(section.containingFile.fileClass(), section.sectionName, property.key)
      if (validator !is GrammarOptionValue) return

      for (region in validator.combinator.labeledRegions(value)) {
        val text = value.substring(region.start, region.end)
        val canonical = canonicalizeIpv6(text) ?: continue // null = not a (pure) IPv6 address
        if (canonical == text) continue
        holder.registerProblem(
          property.valueNode.psi,
          "IPv6 address is not in canonical form (RFC 5952); use '$canonical'",
          ProblemHighlightType.WEAK_WARNING,
          TextRange(region.start, region.end),
          CanonicalizeIpv6QuickFix(region.start, text, canonical),
        )
      }
    }
  }
}
