package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.IPv6MTUBytes
 * C Function: config_parse_mtu(AF_INET6)
 * Used by Options: Network.IPv6MTUBytes
 * 
 * Validates IPv6 MTU values. For IPv6, the minimum MTU is 1280 bytes (IPV6_MIN_MTU)
 * and the maximum is UINT32_MAX (4294967295).
 */
class ConfigParseMtuOptionValue : SimpleGrammarOptionValues(
    "config_parse_mtu",
    SequenceCombinator(
        IntegerTerminal(1280, 4294967296),
        EOF()
    )
)
