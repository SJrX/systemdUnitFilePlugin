package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for CAN.PresumeACK
 * C Function: config_parse_can_control_mode(CAN_CTRLMODE_PRESUME_ACK)
 * Used by Options: CAN.PresumeACK
 */
class ConfigParseCanControlModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_can_control_mode",
    SequenceCombinator(
        BOOLEAN,
        EOF()
    )
)
