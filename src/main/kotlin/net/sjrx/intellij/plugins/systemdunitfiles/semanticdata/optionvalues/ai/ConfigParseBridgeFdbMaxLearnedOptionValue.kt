package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Bridge.FDBMaxLearned
 * C Function: config_parse_bridge_fdb_max_learned(0)
 * Used by Options: Bridge.FDBMaxLearned
 * 
 * Validates an unsigned 32-bit integer value representing the maximum number of learned
 * Ethernet addresses for the bridge. Valid range is 0 to 4294967295 (UINT32_MAX).
 * A value of 0 disables the limit.
 */
class ConfigParseBridgeFdbMaxLearnedOptionValue : SimpleGrammarOptionValues(
    "config_parse_bridge_fdb_max_learned",
    SequenceCombinator(
        IntegerTerminal(0, 4294967296),  // 0 to UINT32_MAX (4294967295), max is exclusive
        EOF()
    )
)
