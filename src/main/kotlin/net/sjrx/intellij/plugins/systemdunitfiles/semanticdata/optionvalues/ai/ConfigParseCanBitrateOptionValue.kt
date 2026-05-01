package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for CAN.BitRate and CAN.DataBitRate.
 * C Function: config_parse_can_bitrate(0) in src/network/networkd-can.c.
 *
 * Internally calls parse_size(rvalue, 1000, &sz), so the value is a decimal bit count
 * optionally suffixed with an SI unit (K, M, G, ...). The result must fit in a uint32_t,
 * i.e. be in the range 1..4294967295.
 */
class ConfigParseCanBitrateOptionValue : SimpleGrammarOptionValues(
    "config_parse_can_bitrate",
    SequenceCombinator(
        RegexTerminal("[0-9]+(?:K|M|G)?", "[0-9]+(?:K|M|G)?"),
        EOF()
    )
)
