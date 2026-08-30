package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ByteSizeTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.comparisonPrefixed
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString
import java.math.BigInteger

/*
 * [Unit] ConditionMemory= / AssertMemory=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionMemory=
 * parser https://github.com/systemd/systemd/blob/048970650c/src/core/load-fragment.c  config_parse_unit_condition_string (CONDITION_MEMORY)
 * check  https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c    condition_test_memory
 */

/**
 * Validator for `[Unit] ConditionMemory=` / `AssertMemory=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_MEMORY. condition_test_memory
 * (src/shared/condition.c) reads an optional comparison operator via parse_compare_operator(&p, 0) —
 * defaulting to `>=` — and then a byte count with parse_size(p, 1024). [ByteSizeTerminal] models that
 * single-element size (base 1024, `K`/`M`/`G`/… suffixes, optional fractional part). The value is
 * only compared against physical memory, so any non-negative size is accepted; the bound here is the
 * full unsigned-64 range parse_size() itself can represent.
 */
class ConfigParseUnitConditionMemoryOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(comparisonPrefixed(ByteSizeTerminal(BigInteger.ZERO, UINT64_MAX)))
) {
    companion object {
        private val UINT64_MAX: BigInteger = BigInteger.TWO.pow(64).subtract(BigInteger.ONE)
    }
}
