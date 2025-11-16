package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for WireGuardPeer.PersistentKeepalive
 * C Function: config_parse_wireguard_keepalive(0)
 * Used by Options: WireGuardPeer.PersistentKeepalive
 * 
 * Accepts either "off" to disable keepalive, or an integer value between 0 and 65535
 * representing the keepalive interval in seconds.
 */
class ConfigParseWireguardKeepaliveOptionValue : SimpleGrammarOptionValues(
    "config_parse_wireguard_keepalive",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("off"),
            IntegerTerminal(0, 65536)  // max is exclusive, so 65536 to include 65535
        ),
        EOF()
    )
)
