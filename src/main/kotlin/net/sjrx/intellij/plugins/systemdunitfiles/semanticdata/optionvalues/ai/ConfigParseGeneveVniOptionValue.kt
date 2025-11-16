package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for GENEVE.Id
 * C Function: config_parse_geneve_vni(0)
 * Used by Options: GENEVE.Id
 *
 * Validates GENEVE Virtual Network Identifier (VNI) values.
 * Valid range: 0 to 16777215 (24-bit value)
 */
class ConfigParseGeneveVniOptionValue : SimpleGrammarOptionValues(
    "config_parse_geneve_vni",
    SequenceCombinator(
        IntegerTerminal(0, 16777216), // 0 to 16777215 inclusive (max exclusive)
        EOF()
    )
)
