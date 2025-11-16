package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Swap.StandardInputText
 * C Function: config_parse_exec_input_text(0)
 * Used by Options: Swap.StandardInputText
 * 
 * This validator accepts arbitrary textual data. The C implementation processes
 * C-style escapes and %-specifiers, but does not reject input based on syntax.
 * Any non-empty string is valid at the syntax level.
 */
class ConfigParseExecInputTextOptionValue : SimpleGrammarOptionValues(
    "config_parse_exec_input_text",
    SequenceCombinator(
        RegexTerminal(".+", ".+"),
        EOF()
    )
)
