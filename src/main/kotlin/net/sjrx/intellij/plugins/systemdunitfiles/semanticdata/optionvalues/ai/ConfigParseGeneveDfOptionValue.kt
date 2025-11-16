package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for GENEVE.IPDoNotFragment
 * C Function: config_parse_geneve_df(0)
 * Used by Options: GENEVE.IPDoNotFragment
 * 
 * Validates the IPDoNotFragment setting for GENEVE tunnels.
 * Accepts: "no", "yes", "inherit" and standard boolean values.
 * The DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN macro allows boolean aliases
 * in addition to the enumerated values.
 */
class ConfigParseGeneveDfOptionValue : SimpleGrammarOptionValues(
    "config_parse_geneve_df",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal(
            "no", "yes", "inherit",
            "1", "y", "true", "t", "on",
            "0", "n", "false", "f", "off"
        ),
        EOF()
    )
)
