package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Socket.DeferTrigger
 * C Function: config_parse_socket_defer_trigger(0), src/core/load-fragment.c
 *
 * DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN over socket_defer_trigger_table
 * (no, yes, patient) in src/core/socket.c.
 * string_table_lookup_from_string_with_boolean tries parse_boolean() (case-insensitive) first,
 * then a case-sensitive lookup in the table, so only the boolean half ignores case.
 */
class ConfigParseSocketDeferTriggerOptionValue : SimpleGrammarOptionValues(
    "config_parse_socket_defer_trigger",
    SequenceCombinator(
        namesOrBoolean("no", "yes", "patient"),
        EOF()
    )
)
