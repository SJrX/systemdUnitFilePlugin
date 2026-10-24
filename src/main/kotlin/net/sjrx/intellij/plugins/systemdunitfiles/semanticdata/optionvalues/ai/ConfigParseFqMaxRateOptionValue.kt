package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import java.math.BigInteger
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [FairQueueing] MaximumRate= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#MaximumRate=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/fq.c#L344-L402   config_parse_fq_max_rate (QDISC_KIND_FQ)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L157-L283   parse_size
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L108-L176   TEST(parse_size_iec)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L178-L246   TEST(parse_size_si)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-fq.network#L16   MaximumRate=1M
 */

/**
 * Validator for [FairQueueing] MaximumRate=.
 *
 * C function: config_parse_fq_max_rate (QDISC_KIND_FQ). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * A rate in bits per second as parse_size(rvalue, 1000) reads it (see [parseSize]) whose byte rate
 * (value / 8) must fit a uint32: above it the parser logs "Specified 'MaximumRate=' is too large,
 * ignoring assignment". So the largest accepted value is 34359738367 bits/s (≈ 34.35G); `34G` is
 * legal and `35G` is not.
 */
class ConfigParseFqMaxRateOptionValue : SimpleGrammarOptionValues(
  "config_parse_fq_max_rate",
  SequenceCombinator(parseSize(1000, maxInclusive = SIZE_UINT32_MAX * BigInteger.valueOf(8) + BigInteger.valueOf(7)), EOF()),
)
