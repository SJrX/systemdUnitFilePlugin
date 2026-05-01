package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Link.WakeOnLan (.link).
 * C Function: config_parse_wol(0)
 *
 * Source: src/shared/ethtool-util.c — config_parse_wol() and the wol_option_map[] table.
 * Accepts either the standalone literal "off", or one or more whitespace-separated
 * Wake-on-LAN flags from the set: phy, unicast, multicast, broadcast, arp, magic, secureon.
 * (Empty value resets the field — handled separately by the inspection framework.)
 */
class ConfigParseWolOptionValue : SimpleGrammarOptionValues(
    "config_parse_wol",
    SequenceCombinator(
        AlternativeCombinator(
            LiteralChoiceTerminal("off"),
            SequenceCombinator(
                LiteralChoiceTerminal("phy", "unicast", "multicast", "broadcast", "arp", "magic", "secureon"),
                ZeroOrMore(
                    SequenceCombinator(
                        WhitespaceTerminal(),
                        LiteralChoiceTerminal("phy", "unicast", "multicast", "broadcast", "arp", "magic", "secureon")
                    )
                )
            )
        ),
        EOF()
    )
)
