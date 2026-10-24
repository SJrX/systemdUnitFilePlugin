package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON_FIELD_NORMALIZED_PATH
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.REST_NORMALIZED_PATH
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [Exec] PivotRoot= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#PivotRoot=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd-nspawn.html#--pivot-root=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L325-L349   config_parse_pivot_root (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-mount.c#L1350-L1383   pivot_root_parse
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188   extract_first_word (":", EXTRACT_DONT_COALESCE_SEPARATORS)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/path-util.c#L1192-L1203   path_is_normalized
 */

/**
 * Validator for the .nspawn `[Exec] PivotRoot=` setting (config_parse_pivot_root, ltype 0).
 *
 * pivot_root_parse() extracts the new root up to the first unescaped `:` (backslash makes the next
 * character literal) and keeps the rest verbatim as the place to pivot the old root to; an empty rest
 * means "unmount the old root". Both paths must be absolute AND normalized — path_is_normalized()
 * refuses `//`, `.` and `..` components, though a trailing slash is fine. The man page only says
 * "absolute"; this follows the C. Invalid values are logged and ignored.
 *
 * Deliberately lenient: path_is_normalized() also caps each component at NAME_MAX bytes and the whole
 * path at PATH_MAX; those limits are not modelled.
 */
class ConfigParsePivotRootOptionValue : SimpleGrammarOptionValues(
    "config_parse_pivot_root",
    SequenceCombinator(
        COLON_FIELD_NORMALIZED_PATH,
        ZeroOrOne(SequenceCombinator(COLON, ZeroOrOne(REST_NORMALIZED_PATH))),
        EOF(),
    )
)
