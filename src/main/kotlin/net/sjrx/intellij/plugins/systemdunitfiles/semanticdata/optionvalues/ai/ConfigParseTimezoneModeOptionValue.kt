package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Exec.Timezone
 * C Function: config_parse_timezone_mode(0)
 * Used by Options: Exec.Timezone
 */
class ConfigParseTimezoneModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_timezone_mode",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("off", "copy", "bind"),
        EOF()
    )
)
