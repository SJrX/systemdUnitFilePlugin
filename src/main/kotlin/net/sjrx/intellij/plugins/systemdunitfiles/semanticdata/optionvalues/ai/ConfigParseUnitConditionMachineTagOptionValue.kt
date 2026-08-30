package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ANY_CONDITION_ARGUMENT
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionMachineTag= / AssertMachineTag=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionMachineTag=
 * parser https://github.com/systemd/systemd/blob/048970650c/src/core/load-fragment.c  config_parse_unit_condition_string (CONDITION_MACHINE_TAG)
 * check  https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c    condition_test_machine_tag
 */

/**
 * Validator for `[Unit] ConditionMachineTag=` / `AssertMachineTag=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_MACHINE_TAG. condition_test_machine_tag
 * (src/shared/condition.c) uses the parameter as an fnmatch() glob against the TAGS from /etc/machine-info.
 * Any non-empty string is a legitimate glob, so nothing beyond non-emptiness can be checked — see
 * [ANY_CONDITION_ARGUMENT].
 */
class ConfigParseUnitConditionMachineTagOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(ANY_CONDITION_ARGUMENT)
)
