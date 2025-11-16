package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for WireGuardPeer.RouteMetric
 * C Function: config_parse_wireguard_peer_route_priority(0)
 * Used by Options: WireGuardPeer.RouteMetric
 */
class ConfigParseWireguardPeerRoutePriorityOptionValue : SimpleGrammarOptionValues(
    "config_parse_wireguard_peer_route_priority",
    SequenceCombinator(
        IntegerTerminal(0, 4294967296),  // 0 to 4294967295 (max is exclusive)
        EOF()
    )
)
