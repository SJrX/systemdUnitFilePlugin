package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Exec.ProcessTwo
 * C Function: config_parse_pid2(0)
 * Used by Options: Exec.ProcessTwo
 */
class ConfigParsePid2OptionValue : SimpleGrammarOptionValues(
    "config_parse_pid2",
    SequenceCombinator(
        BOOLEAN,
        EOF()
    )
)