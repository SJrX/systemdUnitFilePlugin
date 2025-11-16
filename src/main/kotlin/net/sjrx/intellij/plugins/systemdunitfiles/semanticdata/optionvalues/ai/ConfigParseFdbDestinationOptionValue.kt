package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for BridgeFDB.Destination
 * C Function: config_parse_fdb_destination(0)
 * Used by Options: BridgeFDB.Destination
 */
class ConfigParseFdbDestinationOptionValue : SimpleGrammarOptionValues(
    "config_parse_fdb_destination",
    SequenceCombinator(
        AlternativeCombinator(
            IPV4_ADDR,
            IPV6_ADDR
        ),
        EOF()
    )
)
