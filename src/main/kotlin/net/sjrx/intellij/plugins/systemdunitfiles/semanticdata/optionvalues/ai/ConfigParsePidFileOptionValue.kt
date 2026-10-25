package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.ValueWarning
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.ValueWarnings
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.legacyVarRunWarning
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar.NOT_API_VFS
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar.NO_DOT_DOT_COMPONENT
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar.UNIT_TEXT_CHAR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ParseOutcome
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.validate

/*
 * [Service] PIDFile=
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.service.html#PIDFile=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L5702-L5750   config_parse_pid_file (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit-printf.c#L253   unit_path_printf (specifiers expanded first)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34   path_simplify_and_warn (PATH_CHECK_ABSOLUTE | PATH_CHECK_NON_API_VFS)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/path-util.c#L1192   path_is_normalized (no ".." component)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/mountpoint-util.c#L721   path_below_api_vfs (/dev, /sys, /proc)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L574   patch_var_run (/var/run/ is rewritten with a warning, not refused)
 */

/**
 * Validator for `[Service] PIDFile=` (config_parse_pid_file, the only key using it).
 *
 * After specifier expansion a relative path is made absolute by prefixing the runtime directory (/run,
 * or $XDG_RUNTIME_DIR for user services), so both `foo.pid` and `/run/foo.pid` are fine. The result
 * then goes through path_simplify_and_warn(PATH_CHECK_ABSOLUTE | PATH_CHECK_NON_API_VFS): no `..`
 * component anywhere, and an absolute path may not be /dev, /sys, /proc or below them. Unknown
 * specifiers make systemd ignore the setting, so those are flagged too.
 *
 * An empty assignment clears the setting and is handled before this grammar is consulted.
 */
class ConfigParsePidFileOptionValue : SimpleGrammarOptionValues(
  "config_parse_pid_file",
  SequenceCombinator(
    RegexTerminal("""[\s\S]+""", """$NO_DOT_DOT_COMPONENT$NOT_API_VFS$UNIT_TEXT_CHAR+$"""),
    EOF(),
  )
), ValueWarnings {

  /** A path below /var/run/ is rewritten to /run/ by patch_var_run() with a notice. */
  override fun valueWarnings(key: String, value: String): List<ValueWarning> {
    if (combinator.validate(value) != ParseOutcome.Valid) return emptyList()
    return listOfNotNull(legacyVarRunWarning(key, value))
  }
}
