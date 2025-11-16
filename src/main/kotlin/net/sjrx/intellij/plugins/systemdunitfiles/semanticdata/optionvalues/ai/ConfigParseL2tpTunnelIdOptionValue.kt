package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for L2TP.TunnelId
 * C Function: config_parse_l2tp_tunnel_id(0)
 * Used by Options: L2TP.TunnelId
 * 
 * Validates tunnel identifier in the range 1 to 4294967295 (UINT32_MAX).
 */
class ConfigParseL2tpTunnelIdOptionValue : SimpleGrammarOptionValues(
    "config_parse_l2tp_tunnel_id",
    SequenceCombinator(
        IntegerTerminal(1, 4294967296),  // 1 to UINT32_MAX (4294967295) inclusive
        EOF()
    )
)
