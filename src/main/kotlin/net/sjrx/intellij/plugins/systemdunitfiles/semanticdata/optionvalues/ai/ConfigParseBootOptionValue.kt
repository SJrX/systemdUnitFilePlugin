package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Exec.Boot
 * C Function: config_parse_boot(0)
 * Used by Options: Exec.Boot
 */
class ConfigParseBootOptionValue : SimpleGrammarOptionValues(
    "config_parse_boot",
    SequenceCombinator(
        BOOLEAN,
        EOF()
    )
)
