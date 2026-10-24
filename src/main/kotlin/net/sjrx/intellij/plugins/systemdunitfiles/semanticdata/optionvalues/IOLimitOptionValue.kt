package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import java.math.BigInteger


/**
 * IOReadBandwidthMax=, IOWriteBandwidthMax=, IOReadIOPSMax=, IOWriteIOPSMax=: config_parse_io_limit(),
 * https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L4437-L4525
 *
 * The first word (extract_first_word with EXTRACT_UNQUOTE, so a backslash escapes any character) is the device path.
 * It goes through unit_path_printf() and then path_simplify_and_warn() without PATH_CHECK_ABSOLUTE, so it does not
 * have to be absolute, but no ".." component may survive path_simplify() (which only drops leading ones of an
 * absolute path). Everything after it must be `infinity` or a single parse_size() value with base 1000 that is
 * above 0.
 */
class IOLimitOptionValue() : GrammarOptionValue("config_parse_io_limit", GRAMMAR) {

  companion object {
    val GRAMMAR = SequenceCombinator(
      // The device: unquoted characters, backslash escapes, or quoted runs (in which a backslash escapes too).
      ValueChecked(
        RegexTerminal(
          """(?:[^\s"'\\]|\\.|"(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*')+""",
          """(?:[^\s"'\\]|\\.|"(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*')+"""
        )
      ) { text -> devicePathIsAccepted(text) },
      WhitespaceTerminal(),
      PreferValidAlternativeCombinator(
        LiteralChoiceTerminal("infinity"),
        parseSize(base = 1000, minInclusive = BigInteger.ONE, maxInclusive = SafeAtoTerminal.UINT64_MAX, allowInnerWhitespace = true),
      ),
      EOF(),
    )

    /**
     * Whether systemd keeps the device word [raw] (still quoted and escaped as written).
     *
     * An unknown specifier makes unit_path_printf() fail; with a known specifier the expanded path depends on the
     * unit and the machine, so it is accepted. Otherwise path_simplify_and_warn(…, 0) refuses an empty path, "." and
     * any ".." component that path_simplify() keeps.
     * https://github.com/systemd/systemd/blob/0f0682b773/src/shared/specifier.c#L33-L105
     * https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34-L80
     * https://github.com/systemd/systemd/blob/0f0682b773/src/basic/path-util.c#L355-L413
     */
    private fun devicePathIsAccepted(raw: String): Boolean {
      // extract_first_word(…, EXTRACT_UNQUOTE): drop the quotes, a backslash keeps the next character as it is.
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

      // specifier_printf(): %% is a literal '%', a known letter is a real specifier, any other letter or digit fails.
      val expanded = StringBuilder()
      var usesSpecifier = false
      var j = 0
      while (j < word.length) {
        if (word[j] == '%' && j + 1 < word.length) {
          val c = word[j + 1]
          if (c == '%') {
            expanded.append('%')
          } else if (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9') {
            if (c !in BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS) {
              return false
            }
            usesSpecifier = true
          } else {
            expanded.append('%').append(c)
          }
          j += 2
        } else {
          expanded.append(word[j])
          j++
        }
      }
      if (usesSpecifier) {
        return true
      }

      val path = expanded.toString()
      if (path.isEmpty()) {
        return false
      }
      val absolute = path.startsWith("/")
      var components = path.split('/').filter { it.isNotEmpty() && it != "." }
      if (absolute) {
        components = components.dropWhile { it == ".." }
      } else if (components.isEmpty()) {
        // path_simplify() turns "." or "./" into ".", which path_is_normalized() refuses.
        return false
      }
      return components.none { it == ".." }
    }

    val validators = mapOf(
      Validator("config_parse_io_limit", "0") to IOLimitOptionValue()
    )
  }
}
