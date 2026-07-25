package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IntegerTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/**
 * Validator for `[Unit] ConditionCapability=` / `AssertCapability=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_CAPABILITY; the parameter goes
 * to capability_from_name (src/basic/capability-list.c), which accepts either a decimal capability
 * number in 0…CAP_LIMIT (62) or a single capability name. One capability, not a list.
 *
 * The number is tried first so the numeric form isn't swallowed by the name terminal's lenient shape
 * match. Note that systemd's name lookup is gperf-generated with `--ignore-case`, so `cap_sys_admin`
 * also resolves; like the existing CapabilityBoundingSet= validator this grammar lists only the
 * canonical upper-case spellings.
 */
class ConfigParseUnitConditionCapabilityOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(AlternativeCombinator(IntegerTerminal(0, 63), Capabilities))
)
