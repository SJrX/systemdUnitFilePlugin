package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ANY_CONDITION_ARGUMENT
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionEnvironment= / AssertEnvironment=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionEnvironment=
 * parser https://github.com/systemd/systemd/blob/048970650c/src/core/load-fragment.c  config_parse_unit_condition_string (CONDITION_ENVIRONMENT)
 * check  https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c    condition_test_environment
 */

/**
 * Validator for `[Unit] ConditionEnvironment=` / `AssertEnvironment=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_ENVIRONMENT. condition_test_environment
 * (src/shared/condition.c) matches the parameter against the manager's environment block — either a bare
 * variable name or a full `NAME=value`. The parameter is stored and compared verbatim with no validity
 * check, so any non-empty string is legitimate — see [ANY_CONDITION_ARGUMENT].
 */
class ConfigParseUnitConditionEnvironmentOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(ANY_CONDITION_ARGUMENT)
)
