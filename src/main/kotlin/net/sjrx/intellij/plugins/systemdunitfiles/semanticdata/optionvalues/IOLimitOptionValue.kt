package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import java.math.BigInteger


/**
 * IOReadBandwidthMax=, IOWriteBandwidthMax=, IOReadIOPSMax=, IOWriteIOPSMax=: config_parse_io_limit(),
 * https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L4437-L4525
 *
 * The first word (extract_first_word with EXTRACT_UNQUOTE) is the device path. It goes through
 * path_simplify_and_warn() without PATH_CHECK_ABSOLUTE, so it does not have to be absolute. (systemd also rejects a
 * ".." component that path_simplify() cannot drop; that is not checked here.) Everything after it must be
 * `infinity` or a single parse_size() value with base 1000 that is above 0.
 */
class IOLimitOptionValue() : GrammarOptionValue("config_parse_io_limit", GRAMMAR) {

  companion object {
    val GRAMMAR = SequenceCombinator(
      // The device: unquoted characters, backslash escapes, or quoted runs.
      RegexTerminal(
        """(?:[^\s"'\\]|\\\S|"[^"]*"|'[^']*')+""",
        """(?:[^\s"'\\]|\\\S|"[^"]*"|'[^']*')+"""
      ),
      WhitespaceTerminal(),
      PreferValidAlternativeCombinator(
        LiteralChoiceTerminal("infinity"),
        parseSize(base = 1000, minInclusive = BigInteger.ONE, maxInclusive = SafeAtoTerminal.UINT64_MAX, allowInnerWhitespace = true),
      ),
      EOF(),
    )

    val validators = mapOf(
      Validator("config_parse_io_limit", "0") to IOLimitOptionValue()
    )
  }
}
