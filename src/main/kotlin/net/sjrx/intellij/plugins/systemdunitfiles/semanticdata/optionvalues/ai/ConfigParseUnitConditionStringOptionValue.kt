package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * The boolean-valued [Unit] conditions: ConditionFirstBoot=, ConditionACPower= and their Assert twins.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionFirstBoot=
 * parser https://github.com/systemd/systemd/blob/a8e93919c3/src/core/load-fragment.c  config_parse_unit_condition_string
 * checks https://github.com/systemd/systemd/blob/a8e93919c3/src/shared/condition.c    condition_test_first_boot, condition_test_ac_power
 */

/**
 * Validator for the `[Unit]` conditions whose parameter is a plain boolean: `ConditionFirstBoot=` and
 * `ConditionACPower=`, plus their `Assert…=` twins.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_FIRST_BOOT / CONDITION_AC_POWER.
 * condition_test_first_boot and condition_test_ac_power (src/shared/condition.c) both feed the whole
 * parameter to parse_boolean() and treat a parse failure as an error, so nothing else is accepted.
 *
 * The trigger/negate prefix now comes from the shared [conditionString] helper, which spells the
 * marker combinations out as alternatives; the ZeroOrOne-based prefix this class used before could
 * report an error range past the end of the value on inputs like `!!yes`.
 */
class ConfigParseUnitConditionStringOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(BOOLEAN)
)
