package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar
import java.util.regex.Pattern

/**
 * This validator is used for the config_parse_exec validator (ExecStart=, ExecStop=, ExecReload=, ...).
 *
 * Other inspections (e.g. the shell syntax inspection) also use the presence of this validator to decide whether
 * they should run.
 *
 * It reports an error for exactly the command lines that systemd's config_parse_exec() refuses to load, and
 * otherwise a weak warning recommending an absolute path:
 * https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L882-L1094
 */
class ExecOptionValue : OptionValueInformation {
  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  /**
   * Returns problem descriptors.
   * @param property - the Psi Element we are examining.
   * @param holder - A problem holder that we should add to.
   */
  override fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
    val valueText = property.valueText
    if (valueText != null) {
      val error = getErrorMessage(valueText)
      if (error != null) {
        holder.registerProblem(property.valueNode.psi, error, ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
        return
      }
    }

    val values = property.valueNode.text
    val absMatcher = ABSOLUTE_PATH_REGEX.matcher(values)
    if (absMatcher.find()) {
      return
    }
    val m = RELATIVE_PATH_REGEX.matcher(values)
    if (m.find()) {
      val mr = m.toMatchResult()
      val range = TextRange(mr.start(1), mr.end(1))
      holder.registerProblem(
        property.valueNode.psi, "It is recommended to use an absolute path to avoid ambiguity. If the command is not a full (absolute) path, it"
          + " will be resolved to a full path using a fixed search path (that depends on the distribution) determined at compilation time.",
        ProblemHighlightType.WEAK_WARNING, range
      )
    }
  }

  /**
   * Returns why systemd's config_parse_exec() would refuse this command line, or null if it loads it.
   *
   * An unknown specifier (a '%' followed by a letter or digit that unit_full_printf() does not know) in the path or
   * an argument is an error. A path that uses a known specifier is not checked further, because what it expands to
   * depends on the unit name and the machine; a path whose only '%'s are literal (`%%`, or '%' before a character
   * that is not a letter or digit) is checked after expanding them. Warnings that systemd logs while still loading
   * the command (unknown escape sequences, the obsolete `!!` prefix) are not reported.
   */
  override fun getErrorMessage(value: String): String? {
    val rvalue = value.trim(' ', '\t', '\n', '\r')
    var p: Int? = 0

    // https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L913-L1091
    do {
      var semicolon = false

      val first = extractFirstWordAndWarn(rvalue, p) ?: return UNBALANCED_QUOTING
      val firstWord = first.word ?: return null
      p = first.next

      if (firstWord == ";") {
        continue
      }

      // Prefix characters, https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L936-L973
      var ignoreFailure = false
      var separateArgv0 = false
      var noEnvExpand = false
      var viaShell = false
      var fullyPrivileged = false
      var noSetuid = false
      var ambientHack = false
      var f = 0
      while (f < firstWord.length) {
        val c = firstWord[f]
        if (c == '-' && !ignoreFailure) {
          ignoreFailure = true
        } else if (c == '@' && !separateArgv0) {
          separateArgv0 = true
        } else if (c == ':' && !noEnvExpand) {
          noEnvExpand = true
        } else if (c == '|' && !viaShell) {
          viaShell = true
        } else if (c == '+' && !fullyPrivileged && !noSetuid && !ambientHack) {
          fullyPrivileged = true
        } else if (c == '!' && !fullyPrivileged && !noSetuid && !ambientHack) {
          noSetuid = true
        } else if (c == '!' && !fullyPrivileged && !ambientHack) {
          noSetuid = false
          ambientHack = true
        } else {
          break
        }
        f++
      }
      val rawPath = firstWord.substring(f)
      val unknownInPath = firstUnknownSpecifier(rawPath)
      // null when the path uses a real specifier
      val path = expandLiteralSpecifiers(rawPath)

      var argCount = 0
      if (viaShell) {
        // https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L980-L990
        argCount = if (rawPath.isEmpty()) 1 else 2
      } else if (unknownInPath != null) {
        // unit_path_printf() fails with -EBADSLT,
        // https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L992-L998
        return "Unknown specifier %$unknownInPath in executable path: $rawPath"
      } else if (path != null) {
        // https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L992-L1028
        if (path.isEmpty()) {
          return "Empty path in command line"
        }
        if (!stringIsSafe(path)) {
          return "Executable path contains special characters (control characters, quotes, backslashes or *?[): $path"
        }
        if (pathImpliesDirectory(path)) {
          return "Executable path specifies a directory: $path"
        }
        if (!filenameOrAbsolutePathIsValid(path)) {
          return "Neither a valid executable name nor an absolute path: $path"
        }
        if (!separateArgv0) {
          argCount++
        }
      } else {
        // The path is only known once the specifiers are expanded, assume it is fine.
        if (!separateArgv0) {
          argCount++
        }
      }

      // Arguments, https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1031-L1070
      while (p != null && p < rvalue.length) {
        val rest = p
        if (rvalue[rest] == ';' && (rest + 1 == rvalue.length || rvalue[rest + 1] in WHITESPACE)) {
          p = skipWhitespace(rvalue, rest + 1)
          semicolon = true
          break
        }

        if (rvalue[rest] == '\\' && rest + 1 < rvalue.length && rvalue[rest + 1] == ';'
          && (rest + 2 == rvalue.length || rvalue[rest + 2] in WHITESPACE)) {
          p = skipWhitespace(rvalue, rest + 2)
          argCount++
          continue
        }

        val arg = extractFirstWordAndWarn(rvalue, p) ?: return UNBALANCED_QUOTING
        if (arg.word == null) {
          break
        }
        p = arg.next
        // unit_full_printf() fails with -EBADSLT,
        // https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1060-L1066
        val unknown = firstUnknownSpecifier(arg.word)
        if (unknown != null) {
          return "Unknown specifier %$unknown in argument: ${arg.word}"
        }
        argCount++
      }

      // https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1072-L1077
      if (argCount == 0) {
        return "Empty executable name or zeroeth argument: with the '@' prefix the first argument after the path is argv[0]"
      }
    } while (semicolon || firstWord == ";")

    return null
  }

  override val validatorName: String
    get() = VALIDATOR_NAME

  /**
   * One extract_first_word() step: the word, and where the rest starts (null when the input is used up, like
   * systemd's *p = NULL).
   */
  private class Word(val word: String?, val next: Int?)

  companion object {
    // Specifiers that systemd's unit_path_printf() expands to an absolute filesystem path. A command whose first
    // token begins with one of these (e.g. ExecStart=%h/bin/foo) resolves to an absolute path, so it must not be
    // flagged by the "use an absolute path" recommendation below. Text/name specifiers (%i, %n, %H, ...) are
    // deliberately excluded: they do not guarantee an absolute path. See src/core/unit-printf.c in systemd.
    private const val ABSOLUTE_PATH_SPECIFIERS = "hsCdDELStTVyYf"

    // Used to check whether or not there is a problem. A value is considered absolute when, after the optional exec
    // prefixes (+ @ : ! -), it starts with '/' or with a specifier that expands to an absolute path.
    private val ABSOLUTE_PATH_REGEX = Pattern.compile("^\\s*(?:[+@:!-]|!!)*\\s*(?:[\\/]|%[$ABSOLUTE_PATH_SPECIFIERS])")

    // Used to determine what to highlight (we want to avoid highlighting valid prefixes).
    private val RELATIVE_PATH_REGEX = Pattern.compile("^\\s*(?:[+@:!-]+)?([^\\/\\s]\\S*)\\s*")

    // https://github.com/systemd/systemd/blob/0f0682b773/src/fundamental/string-util.h#L15
    private const val WHITESPACE = " \t\n\r"

    // https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L226-L228
    private const val UNBALANCED_QUOTING = "Unbalanced quoting"

    // Marks a \xNN or octal escape above 0x7F: systemd copies it as a raw byte, which is not valid UTF-8 on its own.
    private const val RAW_HIGH_BYTE = '￿'

    // NAME_MAX and PATH_MAX from linux/limits.h
    private const val NAME_MAX = 255
    private const val PATH_MAX = 4096

    const val VALIDATOR_NAME = "config_parse_exec"

    val validators = mapOf(Validator(VALIDATOR_NAME, "*") to ExecOptionValue())

    /**
     * The first '%' + letter/digit pair in [s] that specifier_printf() does not know (it then fails with -EBADSLT),
     * or null. `%%` is a literal '%', and '%' before any other character (or at the end) is kept as it is.
     * https://github.com/systemd/systemd/blob/0f0682b773/src/shared/specifier.c#L33-L105
     */
    private fun firstUnknownSpecifier(s: String): Char? {
      var i = 0
      while (i < s.length) {
        if (s[i] == '%' && i + 1 < s.length) {
          val c = s[i + 1]
          if (c != '%' && c.isAsciiLetterOrDigit() && c !in BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS) {
            return c
          }
          i += 2
        } else {
          i++
        }
      }
      return null
    }

    /**
     * What specifier_printf() makes of [s] if it uses no real specifier (only `%%` and literal '%'s), or null if it
     * uses one, in which case the result depends on the unit and the machine.
     */
    private fun expandLiteralSpecifiers(s: String): String? {
      val out = StringBuilder()
      var i = 0
      while (i < s.length) {
        if (s[i] == '%' && i + 1 < s.length) {
          val c = s[i + 1]
          if (c == '%') {
            out.append('%')
          } else if (c.isAsciiLetterOrDigit()) {
            return null
          } else {
            out.append('%').append(c)
          }
          i += 2
        } else {
          out.append(s[i])
          i++
        }
      }
      return out.toString()
    }

    private fun Char.isAsciiLetterOrDigit(): Boolean = this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9'

    private fun skipWhitespace(s: String, from: Int): Int? {
      var i = from
      while (i < s.length && s[i] in WHITESPACE) {
        i++
      }
      return if (i < s.length) i else null
    }

    /**
     * extract_first_word_and_warn(…, EXTRACT_UNQUOTE|EXTRACT_CUNESCAPE): an invalid escape sequence is only a
     * warning (systemd retries with EXTRACT_UNESCAPE_RELAX and keeps the backslash), so this returns null only for
     * unbalanced quoting.
     * https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L190-L233
     */
    private fun extractFirstWordAndWarn(s: String, from: Int?): Word? {
      return extractFirstWord(s, from, false) ?: extractFirstWord(s, from, true)
    }

    /**
     * extract_first_word() with WHITESPACE separators and EXTRACT_UNQUOTE|EXTRACT_CUNESCAPE (plus
     * EXTRACT_UNESCAPE_RELAX when [unescapeRelax]). Returns null for -EINVAL.
     * https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188
     */
    private fun extractFirstWord(s: String, from: Int?, unescapeRelax: Boolean): Word? {
      var i = from ?: return Word(null, null)
      while (i < s.length && s[i] in WHITESPACE) {
        i++
      }
      if (i >= s.length) {
        return Word(null, null)
      }

      val out = StringBuilder()
      var quote = 0.toChar()
      var backslash = false
      while (true) {
        val atEnd = i >= s.length
        val c = if (atEnd) 0.toChar() else s[i]
        if (backslash) {
          if (atEnd) {
            if (unescapeRelax && quote == 0.toChar()) {
              out.append('\\')
              return Word(out.toString(), null)
            }
            return null
          }
          val consumed = cunescapeOne(s, i, out)
          if (consumed > 0) {
            i += consumed - 1
          } else if (unescapeRelax) {
            out.append('\\').append(c)
          } else {
            return null
          }
          backslash = false
          i++
        } else if (quote != 0.toChar()) {
          if (atEnd) {
            return null
          } else if (c == quote) {
            quote = 0.toChar()
          } else if (c == '\\') {
            backslash = true
          } else {
            out.append(c)
          }
          i++
        } else {
          if (atEnd) {
            return Word(out.toString(), null)
          } else if (c == '\'' || c == '"') {
            quote = c
          } else if (c == '\\') {
            backslash = true
          } else if (c in WHITESPACE) {
            return Word(out.toString(), skipWhitespace(s, i))
          } else {
            out.append(c)
          }
          i++
        }
      }
    }

    /**
     * cunescape_one(…, accept_nul = false) for the escape whose first character (after the backslash) is at [at].
     * Appends the decoded character and returns how many characters were consumed, or -1 for -EINVAL.
     * https://github.com/systemd/systemd/blob/0f0682b773/src/basic/escape.c#L104-L296
     */
    private fun cunescapeOne(s: String, at: Int, out: StringBuilder): Int {
      fun hex(offset: Int, count: Int): Long? {
        if (at + offset + count > s.length) return null
        var v = 0L
        for (k in 0 until count) {
          val d = Character.digit(s[at + offset + k], 16)
          if (d < 0 || s[at + offset + k].code > 0x7f) return null
          v = (v shl 4) or d.toLong()
        }
        return v
      }

      when (val c = s[at]) {
        'a' -> out.append('\u0007')
        'b' -> out.append('\b')
        'f' -> out.append('\u000c')
        'n' -> out.append('\n')
        'r' -> out.append('\r')
        't' -> out.append('\t')
        'v' -> out.append('\u000b')
        '\\', '"', '\'' -> out.append(c)
        's' -> out.append(' ')
        'x' -> {
          val v = hex(1, 2) ?: return -1
          if (v == 0L) return -1
          out.append(if (v > 0x7f) RAW_HIGH_BYTE else v.toInt().toChar())
          return 3
        }
        'u' -> {
          val v = hex(1, 4) ?: return -1
          if (v == 0L || v in 0xD800L..0xDFFFL) return -1
          out.append(v.toInt().toChar())
          return 5
        }
        'U' -> {
          val v = hex(1, 8) ?: return -1
          val invalid = v == 0L || v >= 0x110000L || (v and 0xFFFFF800L) == 0xD800L || v in 0xFDD0L..0xFDEFL
            || (v and 0xFFFEL) == 0xFFFEL
          if (invalid) return -1
          out.appendCodePoint(v.toInt())
          return 9
        }
        in '0'..'7' -> {
          if (at + 3 > s.length) return -1
          val digits = s.substring(at, at + 3)
          if (digits.any { it !in '0'..'7' }) return -1
          val v = digits.toInt(8)
          if (v == 0 || v > 255) return -1
          out.append(if (v > 0x7f) RAW_HIGH_BYTE else v.toChar())
          return 3
        }
        else -> return -1
      }
      return 1
    }

    /**
     * string_is_safe(p, 0): no control characters, newlines, backslashes, quotes or glob characters, valid UTF-8.
     * https://github.com/systemd/systemd/blob/0f0682b773/src/basic/string-util.c#L1154-L1192
     */
    private fun stringIsSafe(p: String): Boolean {
      return p.none { it.code < 0x20 || it.code == 0x7f || it == '\\' || it == '"' || it == '\'' || it in "*?[" || it == RAW_HIGH_BYTE }
    }

    // https://github.com/systemd/systemd/blob/0f0682b773/src/basic/path-util.c#L1340-L1352
    private fun pathImpliesDirectory(path: String): Boolean {
      return path == "." || path == ".." || path.endsWith("/") || path.endsWith("/.") || path.endsWith("/..")
    }

    /**
     * filename_or_absolute_path_is_valid(): an absolute path whose components fit NAME_MAX and whose length fits
     * PATH_MAX, or a single file name that is not "." or "..".
     * https://github.com/systemd/systemd/blob/0f0682b773/src/basic/path-util.h#L156-L161
     * https://github.com/systemd/systemd/blob/0f0682b773/src/basic/path-util.c#L1143-L1190
     */
    private fun filenameOrAbsolutePathIsValid(path: String): Boolean {
      if (path.startsWith("/")) {
        if (path.toByteArray(Charsets.UTF_8).size >= PATH_MAX) return false
        return path.split('/').all { it.toByteArray(Charsets.UTF_8).size <= NAME_MAX }
      }
      return path.isNotEmpty() && path != "." && path != ".." && !path.contains('/') && path.toByteArray(Charsets.UTF_8).size <= NAME_MAX
    }
  }
}
