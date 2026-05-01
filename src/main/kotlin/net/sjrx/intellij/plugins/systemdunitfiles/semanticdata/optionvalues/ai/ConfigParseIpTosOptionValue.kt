package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Socket.IPTOS
 * C Function: config_parse_ip_tos(0)
 *
 * Accepts a symbolic name (low-delay, throughput, reliability, low-cost) or a
 * raw integer in the range 0..255 (ip_tos_from_string falls back to safe_atou
 * with max 0xff).
 */
class ConfigParseIpTosOptionValue : SimpleGrammarOptionValues(
    "config_parse_ip_tos",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("low-delay", "throughput", "reliability", "low-cost"),
            IntegerTerminal(0, 256)
        ),
        EOF()
    )
)
