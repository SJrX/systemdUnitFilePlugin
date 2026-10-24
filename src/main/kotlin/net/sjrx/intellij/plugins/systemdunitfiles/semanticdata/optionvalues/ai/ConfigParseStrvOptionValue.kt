package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RETAIN_ESCAPE_WORD_LIST

/*
 * [Network] BindCarrier= (.network), [Exec] Parameters= / Environment= (.nspawn).
 * (logind.conf's KillOnlyUsers= / KillExcludeUsers= share the parser but are not a file type the plugin
 * handles.)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#BindCarrier=
 *        https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#Parameters=
 *        https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#Environment=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L1416-L1460   config_parse_strv (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188     extract_first_word (EXTRACT_UNQUOTE|EXTRACT_RETAIN_ESCAPE)
 */

/**
 * Validator for config_parse_strv with ltype 0: `[Network] BindCarrier=` in .network files and
 * `[Exec] Parameters=` / `Environment=` in .nspawn files.
 *
 * config_parse_strv splits the value with extract_first_word(EXTRACT_UNQUOTE|EXTRACT_RETAIN_ESCAPE) and
 * appends every word verbatim; it validates nothing about the words themselves (BindCarrier= globs are
 * matched later, nspawn's Environment= entries are not even checked for an `=`). The only input it
 * refuses is one extract_first_word cannot split — an unterminated `"` or `'` quote — and for that it
 * logs a parse error and ignores the assignment. That is all this grammar checks; see
 * [RETAIN_ESCAPE_WORD_LIST] for the word shape.
 */
class ConfigParseStrvOptionValue : SimpleGrammarOptionValues(
    "config_parse_strv",
    RETAIN_ESCAPE_WORD_LIST,
)
