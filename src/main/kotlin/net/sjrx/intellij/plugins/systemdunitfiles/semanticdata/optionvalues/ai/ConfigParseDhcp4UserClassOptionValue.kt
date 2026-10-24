package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CUNESCAPE_UNQUOTE_WORD
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.wordList

/*
 * [DHCPv4] UserClass= (and the old [DHCP] UserClass=) (.network).
 *
 * Note: the plugin reports the deprecated [DHCP] section as not allowed in .network files, so this
 * validator never actually runs for keys there; they are listed only because the gperf shares the parser.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#UserClass=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L655-L698  config_parse_dhcp4_user_class (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188            extract_first_word (EXTRACT_CUNESCAPE|EXTRACT_UNQUOTE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/escape.c#L104-L296                 cunescape_one
 */

/**
 * Validator for `[DHCPv4] UserClass=`: a whitespace-separated list of user-class strings, split by
 * extract_first_word with EXTRACT_CUNESCAPE|EXTRACT_UNQUOTE — so a class may be quoted to contain spaces
 * and may use C escapes ([CUNESCAPE_UNQUOTE_WORD]). An unknown escape, a trailing backslash or an
 * unterminated quote fails the assignment.
 *
 * Each class must also be 1…255 bytes long once unquoted/unescaped (an empty `""` or an over-long one is
 * dropped with a warning); those length checks are not modelled (lenient).
 */
class ConfigParseDhcp4UserClassOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp4_user_class",
    SequenceCombinator(wordList(CUNESCAPE_UNQUOTE_WORD), EOF())
)
