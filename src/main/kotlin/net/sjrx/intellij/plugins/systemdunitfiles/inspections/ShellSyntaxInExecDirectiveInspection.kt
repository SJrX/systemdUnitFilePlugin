package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFile
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileVisitor
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ExecOptionValue
import java.util.regex.Pattern

class ShellSyntaxInExecDirectiveInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val file = holder.file
    return if (file !is UnitFile || !file.getLanguage().isKindOf(UnitFileLanguage.INSTANCE)) {
      PsiElementVisitor.EMPTY_VISITOR
    } else MyVisitor(holder)
  }

  internal enum class LexerTypes(regexp: String) {
    WHITESPACE("\\s+"), STRING_LITERAL("((?<![\\\\])['\"])((?:.(?!(?<![\\\\])\\1))*.?)\\1"), NONSPECIAL_CHARACTERS("[^<>|&\\s]+"), PIPE("\\|"), OUTPUT_REDIRECTS(">{1,2}"), INPUT_REDIRECTS("<{1,2}"), BACKGROUND("&"), SINGLE_CHARACTER(".");

    var pattern: Pattern

    init {
      pattern = Pattern.compile("^$regexp")
    }
  }

  private class MyVisitor(private val holder: ProblemsHolder) : UnitFileVisitor() {
    override fun visitPropertyType(property: UnitFilePropertyType) {
      val section = PsiTreeUtil.getParentOfType(property, UnitFileSectionGroups::class.java) ?: return
      val value = property.valueText ?: return
      val key = property.key
      if (SemanticDataRepository.instance.getOptionValidator(section.sectionName, key) !is ExecOptionValue) return
      val completedString = StringBuilder()
      val im = holder.manager
      while (completedString.length < value.length) {
        for (lexerType in LexerTypes.values()) {
          val searchString = value.substring(completedString.length)
          val m = lexerType.pattern.matcher(searchString)
          if (m.find()) {
            when (lexerType) {
              LexerTypes.PIPE -> {
                val pd = im.createProblemDescriptor(
                  property.valueNode.psi,
                  TextRange.create(completedString.length, m.group(0).length + completedString.length),
                  "systemd does not support piping of output in " + property.key + ". It is recommended you wrap this command in /bin/sh -c",
                  ProblemHighlightType.WARNING,
                  false
                )
                holder.registerProblem(pd)
              }

              LexerTypes.INPUT_REDIRECTS -> {
                val pd = im.createProblemDescriptor(
                  property.valueNode.psi,
                  TextRange.create(completedString.length, m.group(0).length + completedString.length),
                  "systemd does not input redirects in " + property.key + ". It is recommended you wrap this command in /bin/sh -c",
                  ProblemHighlightType.WARNING,
                  false
                )
                holder.registerProblem(pd)
              }

              LexerTypes.OUTPUT_REDIRECTS -> {
                val pd = im.createProblemDescriptor(
                  property.valueNode.psi,
                  TextRange.create(completedString.length, m.group(0).length + completedString.length),
                  "systemd does not redirecting output in " + property.key + ". It is recommended you wrap this command in /bin/sh -c ",
                  ProblemHighlightType.WARNING,
                  false
                )
                holder.registerProblem(pd)
              }

              LexerTypes.BACKGROUND -> {
                val pd = im.createProblemDescriptor(
                  property.valueNode.psi,
                  TextRange.create(completedString.length, m.group(0).length + completedString.length),
                  "systemd does not backgrounding in " + property.key + ". It is recommended you wrap this command in /bin/sh -c ",
                  ProblemHighlightType.WARNING,
                  false
                )
                holder.registerProblem(pd)
              }

              else -> {}
            }
            completedString.append(m.group(0))
            break
          }
        }
      }
    }
  }
}
