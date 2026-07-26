package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CAPABILITY_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.unsignedNumber

/*
 * [Unit] ConditionCapability= / AssertCapability=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionCapability=
 * parser https://github.com/systemd/systemd/blob/a8e93919c3/src/core/load-fragment.c     config_parse_unit_condition_string (CONDITION_CAPABILITY)
 * check  https://github.com/systemd/systemd/blob/a8e93919c3/src/shared/condition.c       condition_test_capability
 * names  https://github.com/systemd/systemd/blob/a8e93919c3/src/basic/capability-list.c  capability_from_name
 *        https://github.com/systemd/systemd/blob/a8e93919c3/src/basic/meson.build        the gperf table is built with --ignore-case
 */

/**
 * Validator for `[Unit] ConditionCapability=` / `AssertCapability=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_CAPABILITY; the parameter goes
 * to capability_from_name (src/basic/capability-list.c), which accepts either a capability number in
 * 0…CAP_LIMIT (62) or a single capability name. One capability, not a list.
 *
 * The number is tried first so the numeric form isn't swallowed by the name terminal's lenient shape
 * match, and it is an [unsignedNumber] because capability_from_name reads it with safe_atoi().
 *
 * Case: the name lookup is gperf-generated with `--ignore-case`
 * (src/basic/meson.build: `gperf … '--ignore-case'`), and capability_to_name() actually renders the
 * canonical form in *lower* case, so `cap_sys_admin` is every bit as valid as `CAP_SYS_ADMIN`.
 * [CAPABILITY_NAME] therefore accepts either, while keeping the upper-case list as the terminal that
 * supplies quick-fix suggestions.
 */
class ConfigParseUnitConditionCapabilityOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(AlternativeCombinator(unsignedNumber(63), CAPABILITY_NAME))
)
