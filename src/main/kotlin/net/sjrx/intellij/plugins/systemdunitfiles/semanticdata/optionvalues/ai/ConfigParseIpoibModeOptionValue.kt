package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for IPoIB.Mode
 * C Function: config_parse_ipoib_mode(0)
 * Used by Options: IPoIB.Mode
 */
class ConfigParseIpoibModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_ipoib_mode",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("datagram", "connected"),
        EOF()
    )
)
