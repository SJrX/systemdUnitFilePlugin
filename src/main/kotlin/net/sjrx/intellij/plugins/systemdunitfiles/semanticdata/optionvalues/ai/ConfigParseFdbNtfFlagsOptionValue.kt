package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for BridgeFDB.AssociatedWith
 * C Function: config_parse_fdb_ntf_flags(0)
 * Used by Options: BridgeFDB.AssociatedWith
 */
class ConfigParseFdbNtfFlagsOptionValue : SimpleGrammarOptionValues(
    "config_parse_fdb_ntf_flags",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("use", "self", "master", "router"),
        EOF()
    )
)
