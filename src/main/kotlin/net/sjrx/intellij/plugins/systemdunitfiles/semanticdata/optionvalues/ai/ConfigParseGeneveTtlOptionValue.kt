package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for GENEVE.TTL
 * C Function: config_parse_geneve_ttl(0)
 * Used by Options: GENEVE.TTL
 * 
 * Accepts either "inherit" or an integer value from 0 to 255 (uint8).
 */
class ConfigParseGeneveTtlOptionValue : SimpleGrammarOptionValues(
    "config_parse_geneve_ttl",
    SequenceCombinator(
        AlternativeCombinator(
            LiteralChoiceTerminal("inherit"),
            IntegerTerminal(0, 256)
        ),
        EOF()
    )
)
