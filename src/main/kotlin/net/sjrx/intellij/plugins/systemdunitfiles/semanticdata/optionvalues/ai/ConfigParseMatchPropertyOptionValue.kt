package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BANG
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CUNESCAPE_WORD
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Checked
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.unquoteWord

/*
 * [Match] Property= (.network and .link).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Property=
 *        https://www.freedesktop.org/software/systemd/man/latest/systemd.link.html#Property=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/net-condition.c#L344-L403   config_parse_match_property (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188     extract_first_word (EXTRACT_CUNESCAPE|EXTRACT_UNQUOTE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/escape.c#L104-L296          cunescape_one
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/env-util.c#L81-L102         env_assignment_is_valid
 */

// env_name_is_valid_n: one or more of VALID_BASH_ENV_NAME_CHARS (letters, digits, `_`), not starting
// with a digit.
private val ENV_ASSIGNMENT = Regex("""[A-Za-z_][A-Za-z0-9_]*=[\s\S]*""")

/**
 * Validator for `[Match] Property=` in .network and .link files.
 *
 * config_parse_match_property strips one optional leading `!` (inverting the match) and splits the rest
 * with extract_first_word(EXTRACT_CUNESCAPE|EXTRACT_UNQUOTE): quotes group, and a backslash starts a C
 * escape (`\"`, `\\`, `\n`, `\x41`, …) both inside and outside quotes — an unknown escape such as `\q`,
 * or an unterminated quote, makes the whole assignment "Invalid syntax, ignoring". Each resulting word
 * must then satisfy env_assignment_is_valid(): `NAME=VALUE` where NAME is letters, digits and `_` not
 * starting with a digit, and VALUE is anything (including empty). A word that fails is logged and
 * skipped — flagged here.
 *
 * The man page's own example is accepted as written:
 * `Property=ID_MODEL_ID=9999 "ID_VENDOR_FROM_DATABASE=vendor name" "KEY=with \"quotation\""`.
 *
 * The NAME check runs on the unquoted word, so quoting the pair, quoting just the value, or even
 * quoting the name are all judged exactly as systemd judges them. Lenient corners: `\x00`/`\u0000`
 * (refused by cunescape_one as NUL) and UTF-16 surrogates in `\u` escapes are not singled out, and
 * VALUE's UTF-8 validity / ARG_MAX length limits are not checked.
 */
class ConfigParseMatchPropertyOptionValue : SimpleGrammarOptionValues(
    "config_parse_match_property",
    AlternativeCombinator(
        // `p += invert` and then extract_first_word() skips leading whitespace, so `! A=b` == `!A=b`.
        SequenceCombinator(BANG, ZeroOrOne(WhitespaceTerminal()), PROPERTY_LIST),
        // A lone `!` yields no words at all, which is accepted.
        SequenceCombinator(BANG, EOF()),
        PROPERTY_LIST,
    )
) {
  private companion object {
    val PROPERTY_WORD = Checked(CUNESCAPE_WORD) { ENV_ASSIGNMENT.matches(unquoteWord(it)) }
    val PROPERTY_LIST = SequenceCombinator(
        PROPERTY_WORD,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), PROPERTY_WORD)),
        EOF(),
    )
  }
}
