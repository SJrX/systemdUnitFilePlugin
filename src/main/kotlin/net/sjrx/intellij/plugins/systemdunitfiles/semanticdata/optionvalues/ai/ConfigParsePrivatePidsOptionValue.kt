package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Swap.PrivatePIDs
 * C Function: config_parse_private_pids(0)
 * Used by Options: Swap.PrivatePIDs
 * 
 * Takes a boolean argument. When enabled, sets up a new PID namespace
 * for the executed processes.
 */
class ConfigParsePrivatePidsOptionValue : SimpleGrammarOptionValues(
    "config_parse_private_pids",
    SequenceCombinator(
        BOOLEAN,
        EOF()
    )
)
