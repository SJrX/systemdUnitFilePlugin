package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.UNIT_PATH
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionPath

/*
 * The path-valued Condition*= / Assert*= settings of a unit's [Unit] section.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionPathExists=
 * parser https://github.com/systemd/systemd/blob/a8e93919c3/src/core/load-fragment.c      config_parse_unit_condition_path
 * checks https://github.com/systemd/systemd/blob/a8e93919c3/src/shared/parse-helpers.c    path_simplify_and_warn
 *        https://github.com/systemd/systemd/blob/a8e93919c3/src/basic/path-util.c         path_is_valid_full, path_is_normalized
 * keys   systemd-build/build/load-fragment-gperf.gperf  (the Unit.Condition… and Unit.Assert… keys)
 *
 * The parser hands the whole rvalue to unit_path_printf() and then to path_simplify_and_warn() with
 * PATH_CHECK_ABSOLUTE. It never splits on whitespace and never unescapes, so the only shape rules are
 * "absolute after specifier expansion" and "no surviving `..` component" — see UNIT_PATH.
 */

/**
 * Validator for every path-valued `Condition…=` / `Assert…=` in `[Unit]`.
 *
 * C function: config_parse_unit_condition_path in src/core/load-fragment.c. It strips an optional
 * `|` (trigger) and then an optional `!` (negate) — with no whitespace allowed after either — runs
 * unit_path_printf() over the rest and requires the result to be absolute
 * (path_simplify_and_warn with PATH_CHECK_ABSOLUTE).
 *
 * Registered under the `*` ltype wildcard because the grammar does not depend on the ConditionType:
 * ConditionPathExists, ConditionPathExistsGlob, ConditionPathIsDirectory, ConditionPathIsSymbolicLink,
 * ConditionPathIsMountPoint, ConditionPathIsReadWrite, ConditionPathIsEncrypted, ConditionPathIsSocket,
 * ConditionDirectoryNotEmpty, ConditionFileNotEmpty, ConditionFileIsExecutable and ConditionNeedsUpdate
 * (plus each Assert… twin) all parse identically. Glob metacharacters need no special handling — they
 * are ordinary path characters here, and the glob is only expanded when the condition is evaluated.
 */
class ConfigParseUnitConditionPathOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_path",
    conditionPath(UNIT_PATH)
)
