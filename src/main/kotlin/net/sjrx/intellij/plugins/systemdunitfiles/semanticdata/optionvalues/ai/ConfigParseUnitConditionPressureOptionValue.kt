package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.PERMYRIAD_PERCENTAGE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionMemoryPressure= / ConditionCPUPressure= / ConditionIOPressure= (and Assert twins).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionMemoryPressure=
 * parser https://github.com/systemd/systemd/blob/048970650c/src/core/load-fragment.c  config_parse_unit_condition_string (CONDITION_*_PRESSURE)
 * check  https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c    condition_test_psi
 */

private val PSI_OPTIONAL_WHITESPACE = ZeroOrOne(WhitespaceTerminal())

// A cgroup slice, e.g. "system.slice" or "user-1000.slice"; taken verbatim (strstrip'd) and handed to
// cg_slice_to_path. Modelled loosely as a run of non-whitespace, non-colon characters.
private val PSI_SLICE = RegexTerminal("[^\\s:]+", "[^\\s:]+")

// The averaging window: condition_test_psi only checks startswith on "10sec"/"1min"/"5min", so any
// trailing text after one of those prefixes is accepted too.
private val PSI_TIMESPAN = RegexTerminal("(?:10sec|1min|5min).*", "(?:10sec|1min|5min).*")

/**
 * Validator for `[Unit] ConditionMemoryPressure=` / `ConditionCPUPressure=` / `ConditionIOPressure=`
 * and their `Assert…=` twins (all three share the single leaf parser condition_test_psi).
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_MEMORY_PRESSURE /
 * CONDITION_CPU_PRESSURE / CONDITION_IO_PRESSURE. condition_test_psi (src/shared/condition.c) parses
 * `[SLICE:]THRESHOLD[/TIMESPAN]`:
 *
 *  - the value is split once on `:` — a leading field, if present, is a cgroup slice, else the check is
 *    against global pressure;
 *  - the threshold is split once on `/` into a percentage (parse_permyriad) and an optional averaging
 *    window that must begin `10sec`, `1min` or `5min`.
 *
 * The `:` and `/` fields are strstrip'd, so whitespace is allowed around those separators but not
 * between the number and its `%` — matching how the kernel-facing parser reads it.
 */
class ConfigParseUnitConditionPressureOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(
        SequenceCombinator(
            ZeroOrOne(SequenceCombinator(PSI_SLICE, PSI_OPTIONAL_WHITESPACE, LiteralChoiceTerminal(":"), PSI_OPTIONAL_WHITESPACE)),
            PERMYRIAD_PERCENTAGE,
            ZeroOrOne(SequenceCombinator(PSI_OPTIONAL_WHITESPACE, LiteralChoiceTerminal("/"), PSI_OPTIONAL_WHITESPACE, PSI_TIMESPAN)),
        )
    )
)
