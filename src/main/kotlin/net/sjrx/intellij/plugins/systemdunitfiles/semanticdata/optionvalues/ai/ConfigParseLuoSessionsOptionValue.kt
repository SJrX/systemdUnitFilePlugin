package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.whitespaceList

/*
 * [Service] LUOSession=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.service.html#LUOSession=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L6847-L6903   config_parse_luo_sessions (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10            extract_first_word (WHITESPACE, flags 0: `\x` → x, quotes literal)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/luo-util.c#L119              luo_session_name_is_valid
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/fd-util.c#L554                fdname_is_valid (ASCII 0x20-0x7E, no ':', <= 255)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/string-util.c#L1154          string_is_safe (STRING_DISALLOW_WHITESPACE: no blanks, backslashes, quotes or `*?[`)
 */

// One name character: printable ASCII except ':' (fdname), and except backslash, quotes and the glob
// characters `*?[` (string_is_safe). The word is extracted with flags 0, so a raw `\x` contributes x.
private const val LUO_CHAR = """[\x21-\x7E&&[^:\\"'*?\[]]"""

/**
 * Validator for `LUOSession=` (config_parse_luo_sessions, ltype 0).
 *
 * A whitespace-separated list of session names, each used as an fd-store FDNAME=: 1–255 printable
 * ASCII characters, none of them `:`, a backslash, a quote, or `*`, `?`, `[`. Invalid names are logged
 * and skipped. Not checked: a name repeated in the same unit ("Duplicate LUO session name, ignoring").
 */
class ConfigParseLuoSessionsOptionValue : SimpleGrammarOptionValues(
  "config_parse_luo_sessions",
  whitespaceList(RegexTerminal("""\S+""", """(?:$LUO_CHAR|\\$LUO_CHAR){1,255}(?!\S)"""))
)
