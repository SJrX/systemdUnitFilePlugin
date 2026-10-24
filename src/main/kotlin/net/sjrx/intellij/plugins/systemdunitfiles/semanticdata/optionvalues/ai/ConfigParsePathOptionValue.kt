package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar.LITERAL_ABSOLUTE_PATH
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * .nspawn [Exec] WorkingDirectory= and [Network] NamespacePath= (also journald.conf TTYPath=, a file
 * type the plugin does not load).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#WorkingDirectory=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#NamespacePath=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L1378-L1414   config_parse_path (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34   path_simplify_and_warn (PATH_CHECK_ABSOLUTE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/path-util.c#L1192   path_is_normalized (no ".." component)
 */

/**
 * Validator for config_parse_path with ltype 0, used by `[Exec] WorkingDirectory=` and
 * `[Network] NamespacePath=` in .nspawn files.
 *
 * The value is taken literally (no specifier expansion) and must be an absolute path with no `..`
 * component; `.` components and repeated slashes are simplified away rather than refused. There is no
 * API VFS restriction, so `NamespacePath=/proc/1/ns/net` is fine. A non-zero ltype would make the error
 * fatal but accepts the same values; no gperf entry uses one today.
 *
 * An empty assignment clears the setting and is handled before this grammar is consulted.
 */
class ConfigParsePathOptionValue : SimpleGrammarOptionValues(
  "config_parse_path",
  SequenceCombinator(LITERAL_ABSOLUTE_PATH, EOF())
)
