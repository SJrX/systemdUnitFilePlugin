package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import net.sjrx.intellij.plugins.systemdunitfiles.intentions.NormalizeMacFormatQuickFix
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFile
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileVisitor
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.fileClass
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SemanticTag
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.formatMac
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.labeledRegions
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings
import net.sjrx.intellij.plugins.systemdunitfiles.settings.PreferredMacFormat

/**
 * Suggests rewriting a 6-byte MAC address to the user's [PreferredMacFormat], behind the grammar flag.
 * It walks the grammar's labeled value spans and, for those tagged [SemanticTag.MAC_ADDRESS], offers a
 * quick fix when the spelling isn't already the preferred one.
 */
class MacAddressFormatInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        if (file !is UnitFile || !file.language.isKindOf(UnitFileLanguage.INSTANCE)) return PsiElementVisitor.EMPTY_VISITOR
        val experimental = ExperimentalSettings.getInstance(file.project).state
        if (!experimental.useGrammarParseEngine) return PsiElementVisitor.EMPTY_VISITOR
        val format = experimental.preferredMacFormat
        if (format == PreferredMacFormat.LEAVE_ALONE) return PsiElementVisitor.EMPTY_VISITOR
        return MyVisitor(holder, format)
    }

    private class MyVisitor(private val holder: ProblemsHolder, private val format: PreferredMacFormat) :
        UnitFileVisitor() {
        override fun visitPropertyType(property: UnitFilePropertyType) {
            val section = PsiTreeUtil.getParentOfType(property, UnitFileSectionGroups::class.java) ?: return
            val value = property.valueText ?: return
            val validator = SemanticDataRepository.instance
                .getOptionValidator(section.containingFile.fileClass(), section.sectionName, property.key)
            if (validator !is GrammarOptionValue) return

            for (region in validator.combinator.labeledRegions(value)) {
                if (region.tag != SemanticTag.MAC_ADDRESS) continue // act only on spans the grammar detects as a Mac address
                val text = value.substring(region.start, region.end)
                val canonical = formatMac(text, format) ?: continue // the correct format for mac
                if (canonical == text) continue
                holder.registerProblem(
                    property.valueNode.psi,
                    "MAC address is not in preferred format, use: '$canonical'",
                    ProblemHighlightType.WEAK_WARNING,
                    TextRange(region.start, region.end),
                    NormalizeMacFormatQuickFix(region.start, text, canonical),
                )
            }
        }
    }
}
