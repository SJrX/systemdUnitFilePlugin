package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for WireGuard.ListenPort
 * C Function: config_parse_wireguard_listen_port(0)
 * Used by Options: WireGuard.ListenPort
 */
class ConfigParseWireguardListenPortOptionValue : SimpleGrammarOptionValues(
    "config_parse_wireguard_listen_port",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("auto"),
            IntegerTerminal(1, 65536)  // Port range 1-65535 (max is exclusive)
        ),
        EOF()
    )
)
