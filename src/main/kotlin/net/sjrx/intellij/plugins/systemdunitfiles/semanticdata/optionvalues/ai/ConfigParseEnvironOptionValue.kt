package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.C_ESCAPE_BODY
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.whitespaceList

/*
 * [Service]/[Socket]/[Mount]/[Swap] Environment=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#Environment=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L2763-L2834   config_parse_environ (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10            extract_first_word (EXTRACT_CUNESCAPE|EXTRACT_UNQUOTE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/escape.c#L104                 cunescape_one (an unknown escape is -EINVAL: the rest of the line is ignored)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit-printf.c#L265             unit_env_printf (specifiers)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/env-util.c#L81                env_assignment_is_valid
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/env-util.c#L31                env_name_is_valid_n (ASCII letters, digits, '_'; not starting with a digit)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-execute/exec-environment.service#L8
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-execute/exec-unsetenvironment.service#L10
 * linux  https://man7.org/linux/man-pages/man7/environ.7.html
 */

// One raw chunk of a word: a double- or single-quoted run, a C escape, or an ordinary character.
// Inside quotes a backslash is an escape too (EXTRACT_CUNESCAPE applies there as well).
private const val QUOTED_SYNTAX = """"(?:[^"\\]|\\[\s\S])*"|'(?:[^'\\]|\\[\s\S])*'"""
private const val QUOTED_SEMANTIC = """"(?:[^"\\]|\\$C_ESCAPE_BODY)*"|'(?:[^'\\]|\\$C_ESCAPE_BODY)*'"""
private const val WORD_SYNTAX = """(?:$QUOTED_SYNTAX|\\\S|[^\s"'\\])+"""
private const val WORD_SEMANTIC = """(?:$QUOTED_SEMANTIC|\\$C_ESCAPE_BODY|[^\s"'\\])+"""

// A specifier stands in for name characters; which specifier, and what it expands to, is not checked.
private const val NAME_SPECIFIER = """%[^\s="'\\]"""

// The plain form NAME=VALUE, where NAME has no quotes or backslashes in it. The name may contain
// specifiers (systemd's own test-execute uses `X%b=%U`).
private val ENV_NAME = RegexTerminal(
  """[^\s="'\\]+""",
  """(?:[A-Za-z_]|$NAME_SPECIFIER)(?:[A-Za-z0-9_]|$NAME_SPECIFIER)*(?![^\s="'\\])""",
)
private val ENV_VALUE = RegexTerminal(WORD_SYNTAX, "$WORD_SEMANTIC(?!\\S)")

// Any word with a quote or backslash before its first '=' — the documented `"NAME=value with spaces"`
// form above all. The assignment only exists after unquoting, so this is one token: the lookahead
// requires that, skipping quotes, the word starts with a valid name followed by '='. Disjoint from the
// plain form (whose name may not contain a quote), so the two never both claim a word.
private val QUOTED_ENV_ASSIGNMENT = RegexTerminal(
  """(?=[^\s="'\\]*["'\\])$WORD_SYNTAX""",
  """(?=["']*(?:[A-Za-z_]|$NAME_SPECIFIER|\\\S)(?:["']|[A-Za-z0-9_]|$NAME_SPECIFIER|\\\S)*=)$WORD_SEMANTIC(?!\S)""",
)

/**
 * Validator for `Environment=` (config_parse_environ, ltype 0).
 *
 * A whitespace-separated list of `NAME=VALUE` assignments, each unquoted (`"VAR1=word1 word2"`,
 * `VAR="a b"`) and C-unescaped before it is checked. NAME is ASCII letters, digits and `_`, not
 * starting with a digit; VALUE may be empty (`FOO=`) and may contain anything, including further `=`.
 * A word without `=` (e.g. the stray `production` in `UNICORN_OPTS=-E production`) is logged and
 * skipped. An unbalanced quote or an unknown escape such as `\q` makes systemd drop the rest of the line.
 *
 * The man page says non-printable characters in values are rejected; env_value_is_valid() at
 * 0f0682b773 only requires valid UTF-8, so control characters (e.g. `\t`) are accepted, following the C.
 *
 * Lenient corners: `%` specifiers are not checked, and in a word that is quoted before its `=` the name
 * is only checked roughly (quotes inside it are skipped over).
 */
class ConfigParseEnvironOptionValue : SimpleGrammarOptionValues(
  "config_parse_environ",
  whitespaceList(
    AlternativeCombinator(
      SequenceCombinator(ENV_NAME, LiteralChoiceTerminal("="), ZeroOrOne(ENV_VALUE)),
      QUOTED_ENV_ASSIGNMENT,
    )
  )
)
