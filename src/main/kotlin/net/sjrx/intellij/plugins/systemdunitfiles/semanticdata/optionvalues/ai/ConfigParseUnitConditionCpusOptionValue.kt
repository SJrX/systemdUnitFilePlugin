package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.comparisonPrefixed
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

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
 * defaulting to `>=` when none is given — and then the number of CPUs with safe_atou() (base 0, no flags):
 * https://github.com/systemd/systemd/blob/0f0682b773/src/shared/condition.c#L417-L441
 *
 * [SafeAtoTerminal] models safe_atou() exactly (#568): besides decimal, `0x` hex and leading-zero octal it
 * also takes a `+` sign, `-0`, and mangle_base()'s `0b`/`0o` prefixes, all of which condition_test_cpus
 * accepts at unit start. The bound is the `unsigned` range.
 */
class ConfigParseUnitConditionCpusOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(
        comparisonPrefixed(
            SafeAtoTerminal(
                signed = false,
                base = 0,
                flags = emptySet(),
                minInclusive = 0L,
                maxInclusive = 4_294_967_295L,
                acceptsWhitespace = true,
                acceptsMinus = true,
            )
        )
    )
)
