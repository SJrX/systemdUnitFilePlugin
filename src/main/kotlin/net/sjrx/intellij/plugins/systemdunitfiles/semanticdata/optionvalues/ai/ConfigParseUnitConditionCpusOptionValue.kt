package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.comparisonPrefixed
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.unsignedNumber

/*
 * [Unit] ConditionCPUs= / AssertCPUs=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionCPUs=
 * parser https://github.com/systemd/systemd/blob/048970650c/src/core/load-fragment.c  config_parse_unit_condition_string (CONDITION_CPUS)
 * check  https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c    condition_test_cpus
 */

/**
 * Validator for `[Unit] ConditionCPUs=` / `AssertCPUs=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_CPUS. condition_test_cpus
 * (src/shared/condition.c) reads an optional comparison operator via parse_compare_operator(&p, 0) —
 * defaulting to `>=` when none is given — and then the number of CPUs with safe_atou(). safe_atou()
 * passes base 0 to strtoul(), so the count may be written decimal, `0x` hex or leading-zero octal;
 * [unsignedNumber] models exactly that, bounded by the `unsigned` range.
 */
class ConfigParseUnitConditionCpusOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(comparisonPrefixed(unsignedNumber(4_294_967_296L)))
)
