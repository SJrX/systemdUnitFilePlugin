package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for ControlledDelay.ECN
 * C Function: config_parse_codel_bool(QDISC_KIND_CODEL)
 * Used by Options: ControlledDelay.ECN
 */
class ConfigParseCodelBoolOptionValue : SimpleGrammarOptionValues(
    "config_parse_codel_bool",
    SequenceCombinator(
        BOOLEAN,
        EOF()
    )
)
