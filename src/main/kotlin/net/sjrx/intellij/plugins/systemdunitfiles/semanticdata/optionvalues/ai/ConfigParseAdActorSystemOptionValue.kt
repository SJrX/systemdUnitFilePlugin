package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Bond.AdActorSystem
 * C Function: config_parse_ad_actor_system(0)
 * Used by Options: Bond.AdActorSystem
 * 
 * Validates 802.3ad system MAC addresses in the format XX:XX:XX:XX:XX:XX
 * where each XX is a hexadecimal byte (00-FF).
 * 
 * Note: The C implementation also validates that the address is not null
 * and not multicast, but those semantic checks cannot be implemented in
 * the grammar layer.
 */
class ConfigParseAdActorSystemOptionValue : SimpleGrammarOptionValues(
    "config_parse_ad_actor_system",
    SequenceCombinator(
        MAC_ADDRESS,
        EOF()
    )
) {
    companion object {
        // MAC address octet: 2 hexadecimal digits
        private val MAC_OCTET = RegexTerminal("[0-9a-fA-F]{2}", "[0-9a-fA-F]{2}")
        private val COLON = LiteralChoiceTerminal(":")
        
        // MAC address format: XX:XX:XX:XX:XX:XX
        val MAC_ADDRESS = SequenceCombinator(
            MAC_OCTET, COLON,
            MAC_OCTET, COLON,
            MAC_OCTET, COLON,
            MAC_OCTET, COLON,
            MAC_OCTET, COLON,
            MAC_OCTET
        )
    }
}
