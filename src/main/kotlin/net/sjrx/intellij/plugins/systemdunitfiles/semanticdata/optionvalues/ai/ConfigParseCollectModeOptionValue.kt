package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Unit.CollectMode
 * C Function: config_parse_collect_mode(0)
 * Used by Options: Unit.CollectMode
 */
class ConfigParseCollectModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_collect_mode",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("inactive", "inactive-or-failed"),
        EOF()
    )
)
