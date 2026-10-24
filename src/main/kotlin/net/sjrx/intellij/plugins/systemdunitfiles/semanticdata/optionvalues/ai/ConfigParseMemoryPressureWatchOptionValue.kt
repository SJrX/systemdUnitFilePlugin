package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Swap.MemoryPressureWatch
 * C Function: config_parse_pressure_watch(0), src/core/load-fragment.c
 *
 * cgroup_pressure_watch_from_string: DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN over
 * cgroup_pressure_watch_table (no, yes, auto, skip) in src/core/cgroup.c.
 * string_table_lookup_from_string_with_boolean tries parse_boolean() (case-insensitive) first,
 * then a case-sensitive lookup in the table, so only the boolean half ignores case.
 */
class ConfigParseMemoryPressureWatchOptionValue : SimpleGrammarOptionValues(
    "config_parse_pressure_watch",
    SequenceCombinator(
        namesOrBoolean("no", "yes", "auto", "skip"),
        EOF()
    )
)
