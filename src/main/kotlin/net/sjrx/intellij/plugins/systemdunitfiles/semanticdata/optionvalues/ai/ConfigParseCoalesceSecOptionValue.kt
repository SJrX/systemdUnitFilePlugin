package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Link.{RxCoalesceSec, RxCoalesceIrqSec, TxCoalesceSec, TxCoalesceIrqSec,
 *                    StatisticsBlockCoalesceSec, RxCoalesceLowSec, TxCoalesceLowSec,
 *                    RxCoalesceHighSec, TxCoalesceHighSec, CoalescePacketRateSampleIntervalSec}.
 * C Function: config_parse_coalesce_sec(0)
 *
 * Per src/shared/ethtool-util.c, the rvalue is parsed via parse_sec() which accepts a
 * non-negative integer with an optional time unit suffix (defaulting to seconds when
 * no suffix is present). The C code further rejects values exceeding UINT32_MAX usec
 * and zero values for two specific keys, but those numeric checks cannot be enforced
 * at the grammar level here (the same validator is shared across all ten keys).
 */
class ConfigParseCoalesceSecOptionValue : SimpleGrammarOptionValues(
    "config_parse_coalesce_sec",
    SequenceCombinator(
        RegexTerminal(
            "[0-9]+(?:ms|us|µs|s|m|h|d|w|y)?",
            "[0-9]+(?:ms|us|µs|s|m|h|d|w|y)?"
        ),
        EOF()
    )
)
