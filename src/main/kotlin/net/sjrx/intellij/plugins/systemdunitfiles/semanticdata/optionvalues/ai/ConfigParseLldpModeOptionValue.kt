package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.LLDP
 * C Function: config_parse_lldp_mode(0), src/network/networkd-lldp-rx.c
 *
 * DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN over lldp_mode_table (no, yes, routers-only).
 * string_table_lookup_from_string_with_boolean tries parse_boolean() (case-insensitive) first,
 * then a case-sensitive lookup in the table, so only the boolean half ignores case.
 */
class ConfigParseLldpModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_lldp_mode",
    SequenceCombinator(
        PreferValidAlternativeCombinator(
            FlexibleLiteralChoiceTerminal("routers-only"),
            BOOLEAN,
        ),
        EOF()
    )
)
