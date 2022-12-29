package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import net.sjrx.intellij.plugins.systemdunitfiles.intentions.SwapPrefixOrderQuickFix
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

class NamespacePathOptionValue : OptionValueInformation {

  override fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
    val value = property.valueNode.text

    val matches = NEXT_WORD_REGEX.findAll(value)

    for (match in matches) {
      val text = match.groups[0]!!.value
      val textRange = match.groups[0]!!.toTextRange()

      if (text.startsWith("/")) {
        // Absolute Path
        continue
      }

      if (text.startsWith("+/")) {
        // Relative to root directory of unit
        continue
      }

      if (text.startsWith("-/")) {
        // Ignore if directory doesn't exist.
        continue
      }

      if (text.startsWith("-+/")) {
        // Options in right order, and absolute
        continue
      }

      if (text.startsWith("+-")) {
        // Options in wrong order, but absolute directory

        val quickFix = SwapPrefixOrderQuickFix(textRange.startOffset)

        holder.registerProblem(
          property.valueNode.psi,
          "Invalid Prefix [+-], if both + and - are specified, then - must be before +.",
          ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
          TextRange.create(textRange.startOffset, textRange.startOffset + 2),
          quickFix
          )

        if (text.startsWith("+-/")) {
          // Absolute Path, no more problems
          continue
        }
      }

      var incrementOffset = 0
      if (text.startsWith("+-") || text.startsWith("-+")) {
        incrementOffset += 2
      } else if (text.startsWith("+") || text.startsWith("-")) {
        incrementOffset += 1
      }

      val problemRange = TextRange.create(textRange.startOffset + incrementOffset, textRange.endOffset)
      holder.registerProblem(property.valueNode.psi, "Invalid Path [${text.substring(incrementOffset)}] all paths must be absolute", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, problemRange)
    }
  }

  override fun getAutoCompleteOptions(project: Project): Set<String> {
    // Adding auto complete here is annoying because each space needs to be considered a different path, maybe one day.
    return emptySet()
  }



  override fun getErrorMessage(value: String): String? {
    throw IllegalStateException("This method shouldn't be called")
  }

  override val validatorName: String
    get() = VALIDATOR_NAME

  companion object {
    const val VALIDATOR_NAME = "config_parse_namespace_path_strv"

    val NEXT_WORD_REGEX = """(?:[^\\ ]|\\\s|(?:\\\\))+""".toRegex()

    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to NamespacePathOptionValue())
  }

}

private fun MatchGroup.toTextRange() : TextRange {
  return TextRange.create(this.range.first, this.range.last + 1)
}
