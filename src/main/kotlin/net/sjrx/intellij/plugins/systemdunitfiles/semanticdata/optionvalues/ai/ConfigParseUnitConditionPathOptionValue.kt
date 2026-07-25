package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CONDITION_PATH
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionPath

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
    conditionPath(CONDITION_PATH)
)
