package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ANY_CONDITION_ARGUMENT
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionHost= / AssertHost=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionHost=
 * parser https://github.com/systemd/systemd/blob/048970650c/src/core/load-fragment.c  config_parse_unit_condition_string (CONDITION_HOST)
 * check  https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c    condition_test_host
 */

/**
 * Validator for `[Unit] ConditionHost=` / `AssertHost=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_HOST. condition_test_host
 * (src/shared/condition.c) treats the parameter either as a 128-bit machine/boot/product ID or, failing
 * that, as an fnmatch() glob against the hostname. Any non-empty string is a legitimate glob, so there
 * is no shape to enforce beyond non-emptiness — see [ANY_CONDITION_ARGUMENT].
 */
class ConfigParseUnitConditionHostOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(ANY_CONDITION_ARGUMENT)
)
