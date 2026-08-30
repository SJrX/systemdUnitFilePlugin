package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ANY_CONDITION_ARGUMENT
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionKernelCommandLine= / AssertKernelCommandLine=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionKernelCommandLine=
 * parser https://github.com/systemd/systemd/blob/048970650c/src/core/load-fragment.c  config_parse_unit_condition_string (CONDITION_KERNEL_COMMAND_LINE)
 * check  https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c    condition_test_kernel_command_line
 */

/**
 * Validator for `[Unit] ConditionKernelCommandLine=` / `AssertKernelCommandLine=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_KERNEL_COMMAND_LINE.
 * condition_test_kernel_command_line (src/shared/condition.c) matches the parameter against the words on
 * the kernel command line — either a bare option name or a full `name=value`. Any non-empty string is a
 * legitimate parameter, so only non-emptiness can be checked — see [ANY_CONDITION_ARGUMENT].
 */
class ConfigParseUnitConditionKernelCommandLineOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(ANY_CONDITION_ARGUMENT)
)
