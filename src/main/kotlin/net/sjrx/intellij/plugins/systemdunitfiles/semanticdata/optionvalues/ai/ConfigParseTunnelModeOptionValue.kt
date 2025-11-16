package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Tunnel.Mode
 * C Function: config_parse_tunnel_mode(0)
 * Used by Options: Tunnel.Mode
 * 
 * Validates tunnel mode values based on the tunnel_mode_table in systemd.
 * Valid values: any, ipip, ip6ip, ipip6, ip6ip6
 */
class ConfigParseTunnelModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_tunnel_mode",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("any", "ipip", "ip6ip", "ipip6", "ip6ip6"),
        EOF()
    )
)