package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RETAIN_ESCAPE_WORD_LIST

/*
 * [Match] Path= / Driver= / Type= / Kind= (.network and .link), WLANInterfaceType= / SSID= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Path=
 *        https://www.freedesktop.org/software/systemd/man/latest/systemd.link.html#Path=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/net-condition.c#L228-L281   config_parse_match_strv (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188     extract_first_word (EXTRACT_UNQUOTE|EXTRACT_RETAIN_ESCAPE)
 */

/**
 * Validator for the glob-list `[Match]` settings: `Path=`, `Driver=`, `Type=`, `Kind=` (.network and
 * .link) and `WLANInterfaceType=`, `SSID=` (.network).
 *
 * config_parse_match_strv strips one optional leading `!` (inverting the whole list) and then splits the
 * rest with extract_first_word(EXTRACT_UNQUOTE|EXTRACT_RETAIN_ESCAPE). Each word is an fnmatch() glob
 * stored verbatim — the kind, type and driver names are open-ended (the man page itself says the lists of
 * examples are "not comprehensive"), so no word is checked against a list.
 *
 * What *is* checked is the one thing systemd rejects: an unterminated `"` or `'` quote makes
 * extract_first_word fail, and the parser then logs "Invalid syntax, ignoring" and drops the whole
 * assignment. Quoted pieces may be glued to bare text (`"pci-0000 00"*`), and because of
 * EXTRACT_RETAIN_ESCAPE a backslash is an ordinary character, even inside quotes. The `!` needs no
 * special handling: it is an ordinary first character of the first word, and `!` on its own is accepted
 * (it yields an empty list).
 */
class ConfigParseMatchStrvOptionValue : SimpleGrammarOptionValues(
    "config_parse_match_strv",
    RETAIN_ESCAPE_WORD_LIST,
)
