package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for WLAN.Type
 * C Function: config_parse_wlan_iftype(0)
 * Used by Options: WLAN.Type
 */
class ConfigParseWlanIftypeOptionValue : SimpleGrammarOptionValues(
    "config_parse_wlan_iftype",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal(
            "ad-hoc",
            "station",
            "ap",
            "ap-vlan",
            "wds",
            "monitor",
            "mesh-point",
            "p2p-client",
            "p2p-go",
            "p2p-device",
            "ocb",
            "nan"
        ),
        EOF()
    )
)
