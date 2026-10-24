package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [TokenBucketFilter] Rate=, PeakRate= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Rate=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#PeakRate=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/tbf.c#L184-L243   config_parse_tbf_rate (QDISC_KIND_TBF)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L157-L283   parse_size
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L108-L176   TEST(parse_size_iec)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L178-L246   TEST(parse_size_si)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-tbf.network#L12-L16   Rate=1G, PeakRate=100G
 */

/**
 * Validator for [TokenBucketFilter] Rate=, PeakRate=.
 *
 * C function: config_parse_tbf_rate (QDISC_KIND_TBF). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * A rate in bits per second as parse_size(rvalue, 1000) reads it: a number with an optional SI suffix
 * `K`/`M`/`G`/`T`/`P`/`E` (upper case only, powers of 1000) or `B`, optionally several descending
 * elements (`1G 500M`). The result is divided by 8 and stored as a uint64; there is no further bound.
 * Note `10Mbit` / `10mbit` (tc(8) spellings) are NOT accepted by systemd.
 */
class ConfigParseTbfRateOptionValue : SimpleGrammarOptionValues(
  "config_parse_tbf_rate",
  SequenceCombinator(parseSize(1000), EOF()),
)
