package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/**
 * config_parse_unit_path_printf: Mount.Where=, Automount.Where=, RootDirectory=, RootImage=, TTYPath=, ...
 *
 * [config_parse_unit_path_printf](https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L428-L458)
 * expands the unit specifiers and hands the result to
 * [config_parse_path](https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L1378-L1414), which runs
 * [path_simplify_and_warn](https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34-L79) with
 * PATH_CHECK_ABSOLUTE: the path must be absolute and, once `.` components and repeated slashes are simplified away,
 * have no `..` component. Spaces and a lone `/` are fine (`Where=/hoge/with space`, `Where=/`). A value that starts
 * with a specifier (`%h/x`) is accepted, because what it expands to is only known when the unit is loaded; an
 * unknown specifier is refused. ltype `true` only makes the same errors fatal.
 */
class PathOptionValue : SimpleGrammarOptionValues(VALIDATOR_NAME, SequenceCombinator(BatchUnitCoreGrammar.UNIT_ABSOLUTE_PATH, EOF())) {

  override fun invalidValueMessage(key: String, badValue: String): String =
    "$key must be an absolute path with no .. component, not [$badValue]"

  companion object {
    const val VALIDATOR_NAME = "config_parse_unit_path_printf"

    val validators = mapOf(
      Validator(VALIDATOR_NAME, "0") to PathOptionValue(),
      Validator(VALIDATOR_NAME, "true") to PathOptionValue(),
                           )
  }
}
