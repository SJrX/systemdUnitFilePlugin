package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Unit.FailureActionExitStatus, Unit.SuccessActionExitStatus
 * C Function: config_parse_exit_status(0)
 * Used by Options: Unit.FailureActionExitStatus, Unit.SuccessActionExitStatus
 * 
 * Validates exit status values in the range 0-255 (uint8_t range).
 * The C implementation uses safe_atou8() which parses an unsigned 8-bit integer.
 */
class ConfigParseExitStatusOptionValue : SimpleGrammarOptionValues(
    "config_parse_exit_status",
    SequenceCombinator(
        IntegerTerminal(0, 256),  // Range 0-255 (max is exclusive)
        EOF()
    )
)
