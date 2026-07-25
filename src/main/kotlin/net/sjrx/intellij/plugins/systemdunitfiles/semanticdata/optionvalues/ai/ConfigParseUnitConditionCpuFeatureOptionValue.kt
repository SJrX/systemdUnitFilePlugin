package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/**
 * Validator for `[Unit] ConditionCPUFeature=` / `AssertCPUFeature=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_CPU_FEATURE;
 * condition_test_cpufeature (src/shared/condition.c) splits an optional `<arch>.` prefix off the
 * parameter and looks the rest up with has_cpu_with_flag(), which scans the `flags` line of
 * /proc/cpuinfo.
 *
 * That flag set is whatever the running CPU reports, so there is no list to check against and this
 * grammar deliberately only pins the shape: exactly one whitespace-free token after the optional
 * trigger/negate markers. That much *is* checkable — the parameter is never split, so
 * `ConditionCPUFeature=sse2 avx` asks for a single feature literally named "sse2 avx" and can never
 * be true.
 *
 * (This replaces an earlier mapping of CONDITION_CPU_FEATURE onto the boolean condition grammar,
 * which rejected every real feature name.)
 */
class ConfigParseUnitConditionCpuFeatureOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(RegexTerminal("""\S+""", """[A-Za-z0-9_.\-]+"""))
)
