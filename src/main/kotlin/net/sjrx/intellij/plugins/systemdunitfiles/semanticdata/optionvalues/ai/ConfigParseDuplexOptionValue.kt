package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Link.Duplex
 * C Function: config_parse_duplex(0)
 * Used by Options: Link.Duplex
 */
class ConfigParseDuplexOptionValue : SimpleGrammarOptionValues(
    "config_parse_duplex",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("full", "half"),
        EOF()
    )
)
