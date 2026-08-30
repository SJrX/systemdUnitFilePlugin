package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionKernelModuleLoaded= / AssertKernelModuleLoaded=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionKernelModuleLoaded=
 * parser https://github.com/systemd/systemd/blob/048970650c/src/core/load-fragment.c  config_parse_unit_condition_string (CONDITION_KERNEL_MODULE_LOADED)
 * check  https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c    condition_test_kernel_module_loaded
 *        https://github.com/systemd/systemd/blob/048970650c/src/basic/path-util.c     filename_is_valid
 */

/**
 * Validator for `[Unit] ConditionKernelModuleLoaded=` / `AssertKernelModuleLoaded=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_KERNEL_MODULE_LOADED.
 * condition_test_kernel_module_loaded (src/shared/condition.c) first replaces every `-` with `_` and
 * then reports "not loaded" for any name filename_is_valid() rejects. filename_is_valid()
 * (src/basic/path-util.c) requires: non-empty, not "." or "..", no "/", at most NAME_MAX (255) bytes.
 *
 * Because `-` is normalised to `_` before the check, a dash never turns a name into a reserved one, so
 * only a bare `.` or `..` is refused. No character class beyond "no slash" is imposed by the parser.
 */
class ConfigParseUnitConditionKernelModuleLoadedOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(
        RegexTerminal(".+", "(?!\\.\\.?\$)[^/\\x00]{1,255}")
    )
)
