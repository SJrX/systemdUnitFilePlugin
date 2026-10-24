package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import java.math.BigInteger
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [EnhancedTransmissionSelection] QuantumBytes= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#QuantumBytes=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/ets.c#L144-L225   config_parse_ets_quanta (QDISC_KIND_ETS)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L157-L283   parse_size
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/include/uapi/linux/pkt_sched.h#L1210   TCQ_ETS_MAX_BANDS = 16
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L108-L176   TEST(parse_size_iec)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L178-L246   TEST(parse_size_si)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-ets.network#L14-L17   QuantumBytes=2 4 6 / QuantumBytes= / QuantumBytes=1001 1002 1003
 */

private val ETS_QUANTUM = parseSize(1024, minInclusive = BigInteger.ONE, maxInclusive = SIZE_UINT32_MAX, allowInnerWhitespace = false)

/**
 * Validator for [EnhancedTransmissionSelection] QuantumBytes=.
 *
 * C function: config_parse_ets_quanta (QDISC_KIND_ETS). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * A whitespace-separated list (extract_first_word, no flags) of byte sizes, each read with
 * parse_size(word, 1024) and required to be 1…4294967295; at most TCQ_ETS_MAX_BANDS (16) entries. A
 * zero, oversized or unparsable entry is logged and skipped by the C ("ignoring assignment"), and so is
 * flagged here; so is a 17th entry on one line.
 *
 * Because the list is split on whitespace first, each entry is a single word: `1K` or `1K512B` is one
 * entry, but `1K 512B` is two.
 *
 * Lenient: the 16-entry cap is cumulative across repeated QuantumBytes= lines (until an empty
 * assignment resets the list); only the per-line count is checked.
 */
class ConfigParseEtsQuantaOptionValue : SimpleGrammarOptionValues(
  "config_parse_ets_quanta",
  SequenceCombinator(ETS_QUANTUM, Repeat(SequenceCombinator(WhitespaceTerminal(), ETS_QUANTUM), 0, 15), EOF()),
)
