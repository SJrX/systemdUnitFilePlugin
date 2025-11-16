package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Swap.RuntimeDirectoryPreserve
 * C Function: config_parse_exec_preserve_mode(0)
 * Used by Options: Swap.RuntimeDirectoryPreserve
 * 
 * Validates the preserve mode for runtime directories:
 * - no: directories always removed when service stops
 * - restart: directories preserved on automatic and manual restart
 * - yes: directories not removed when service stops
 */
class ConfigParseExecPreserveModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_exec_preserve_mode",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("no", "restart", "yes"),
        EOF()
    )
)
