package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ANY_CONDITION_ARGUMENT
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionKernelVersion= / ConditionVersion= (and their Assert twins).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionKernelVersion=
 * parser https://github.com/systemd/systemd/blob/048970650c/src/core/load-fragment.c  config_parse_unit_condition_string (CONDITION_VERSION)
 * check  https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c    condition_test_version, condition_test_version_cmp
 */

/**
 * Validator for `[Unit] ConditionKernelVersion=` / `ConditionVersion=` / `AssertKernelVersion=` /
 * `AssertVersion=` (all share ltype CONDITION_VERSION).
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_VERSION. condition_test_version /
 * condition_test_version_cmp (src/shared/condition.c) accept a whitespace-separated list of
 * `[operator] value` expressions, but when an expression carries no comparison operator it is treated as
 * a plain fnmatch glob — and any non-empty token is a valid glob. There is therefore no non-empty value
 * the parser rejects, so the honest model is non-emptiness — see [ANY_CONDITION_ARGUMENT].
 */
class ConfigParseUnitConditionVersionOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(ANY_CONDITION_ARGUMENT)
)
