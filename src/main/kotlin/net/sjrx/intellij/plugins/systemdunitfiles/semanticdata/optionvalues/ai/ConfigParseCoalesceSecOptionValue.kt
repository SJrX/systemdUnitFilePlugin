package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Link.{RxCoalesceSec, RxCoalesceIrqSec, TxCoalesceSec, TxCoalesceIrqSec,
 *                    StatisticsBlockCoalesceSec, RxCoalesceLowSec, TxCoalesceLowSec,
 *                    RxCoalesceHighSec, TxCoalesceHighSec, CoalescePacketRateSampleIntervalSec}.
 * C Function: config_parse_coalesce_sec(0)
 *
 * Calls parse_sec, which accepts "infinity", a fractional or integer number with any
 * of systemd's time-unit suffixes, and compound forms like "1h 30s".
 */
class ConfigParseCoalesceSecOptionValue : SimpleGrammarOptionValues(
    "config_parse_coalesce_sec",
    SequenceCombinator(TIME_VALUE, EOF())
)
