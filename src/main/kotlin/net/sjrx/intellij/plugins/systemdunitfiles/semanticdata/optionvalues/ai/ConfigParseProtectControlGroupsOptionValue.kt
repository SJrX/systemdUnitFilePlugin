package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for ProtectControlGroups= (Service/Socket/Mount/Swap)
 * C Function: config_parse_protect_control_groups(0), src/core/load-fragment.c
 *
 * DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN over protect_control_groups_table
 * (no, yes, private, strict) in src/core/namespace.c.
 * string_table_lookup_from_string_with_boolean tries parse_boolean() (case-insensitive) first,
 * then a case-sensitive lookup in the table, so only the boolean half ignores case.
 */
class ConfigParseProtectControlGroupsOptionValue : SimpleGrammarOptionValues(
    "config_parse_protect_control_groups",
    SequenceCombinator(
        namesOrBoolean("no", "yes", "private", "strict"),
        EOF()
    )
)
