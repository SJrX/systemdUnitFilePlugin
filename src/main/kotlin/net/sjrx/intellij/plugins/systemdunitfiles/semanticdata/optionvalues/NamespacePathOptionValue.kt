package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import net.sjrx.intellij.plugins.systemdunitfiles.intentions.SwapPrefixOrderQuickFix
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar

class NamespacePathOptionValue : OptionValueInformation {

  override fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
    val value = property.valueNode.text

    val matches = NEXT_WORD_REGEX.findAll(value)

    for (match in matches) {
      val raw = match.groups[0]!!.value
      val textRange = match.groups[0]!!.toTextRange()

      // extract_first_word(…, EXTRACT_UNQUOTE) drops quotes and keeps the character after a backslash.
      // https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L5067-L5100
      val text = unquote(raw)

      // After the optional "-" and "+" prefixes the path goes through unit_path_printf(): an unknown specifier (%Q)
      // is refused, a path that starts with a known one (%h, %S, ...) is only known once it is expanded, and %% is a
      // literal '%'. Then path_simplify_and_warn(PATH_CHECK_ABSOLUTE) refuses a ".." component path_simplify()
      // keeps (https://github.com/systemd/systemd/blob/0f0682b773/src/shared/specifier.c#L33-L105,
      // https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34-L80).
      val prefixLength = if (text.startsWith("-+")) 2 else if (text.startsWith("-") || text.startsWith("+")) 1 else 0
      val path = text.substring(prefixLength)
      if (!KNOWN_SPECIFIERS_ONLY.matches(path)) {
        holder.registerProblem(
          property.valueNode.psi,
          "Invalid Path [$path] contains an unknown specifier",
          ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
          textRange
        )
        continue
      }
      if (STARTS_WITH_SPECIFIER.matches(path)) {
        continue
      }
      if (path.startsWith("/") && !NO_DOT_DOT_AFTER_SIMPLIFY.matches(path)) {
        holder.registerProblem(
          property.valueNode.psi,
          "Invalid Path [$path] is not normalized: a '..' component is not allowed",
          ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
          textRange
        )
        continue
      }

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

    // A word: anything but whitespace or quotes, a backslash escaping the next character, or a quoted run (which may
    // contain whitespace). A backslash at the end of a line is a line continuation, not part of a word.
    val NEXT_WORD_REGEX = """(?:[^\\\s"']|\\[^\r\n]|"(?:[^"\\]|\\[^\r\n])*"|'(?:[^'\\]|\\[^\r\n])*')+""".toRegex()

    // Every '%' is "%%", a known specifier, or a '%' that specifier_printf() keeps as it is.
    private val KNOWN_SPECIFIERS_ONLY = "${BatchUnitCoreGrammar.UNIT_TEXT_CHAR}*".toRegex()

    private val STARTS_WITH_SPECIFIER = "%[${BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS}].*".toRegex()

    // An absolute path without a ".." component after the leading ones path_simplify() drops, or one that uses a known
    // specifier (the expansion decides).
    private val NO_DOT_DOT_AFTER_SIMPLIFY =
      ("/(?:\\.{0,2}(?:/|$))*(?!(?:.*/)?\\.\\.(?:/|$)).*" +
        "|/(?=(?:[^%]|%%|%(?![A-Za-z0-9%]))*%[${BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS}]).*").toRegex()

    private fun unquote(raw: String): String {
      val word = StringBuilder()
      var quote = 0.toChar()
      var i = 0
      while (i < raw.length) {
        val c = raw[i]
        if (c == '\\' && i + 1 < raw.length) {
          word.append(raw[i + 1])
          i += 2
          continue
        }
        if (quote == 0.toChar() && (c == '"' || c == '\'')) {
          quote = c
        } else if (c == quote) {
          quote = 0.toChar()
        } else {
          word.append(c)
        }
        i++
      }
      return word.toString()
    }

    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to NamespacePathOptionValue())
  }

}

private fun MatchGroup.toTextRange() : TextRange {
  return TextRange.create(this.range.first, this.range.last + 1)
}
