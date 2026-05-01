package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for CAN.BitRate and CAN.DataBitRate.
 * C Function: config_parse_can_bitrate(0) in src/network/networkd-can.c.
 *
 * Internally calls parse_size(rvalue, 1000, &sz). parse_size accepts a decimal
 * number (with optional fractional part) optionally suffixed with B/K/M/G/T/P/E.
 * The result must fit in a uint32_t; the range check is not enforced here.
 */
class ConfigParseCanBitrateOptionValue : SimpleGrammarOptionValues(
    "config_parse_can_bitrate",
    SequenceCombinator(
        RegexTerminal(
            "[0-9]+(?:\\.[0-9]+)?\\s*[BKMGTPE]?",
            "[0-9]+(?:\\.[0-9]+)?\\s*[BKMGTPE]?"
        ),
        EOF()
    )
)
