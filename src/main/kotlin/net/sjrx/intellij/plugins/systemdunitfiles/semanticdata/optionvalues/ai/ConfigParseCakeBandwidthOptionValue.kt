package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [CAKE] Bandwidth= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Bandwidth=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/cake.c#L141-L193   config_parse_cake_bandwidth (QDISC_KIND_CAKE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L157-L283   parse_size
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L108-L176   TEST(parse_size_iec)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L178-L246   TEST(parse_size_si)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-cake.network#L12   Bandwidth=500M
 */

/**
 * Validator for [CAKE] Bandwidth=.
 *
 * C function: config_parse_cake_bandwidth (QDISC_KIND_CAKE). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * A rate in bits per second as parse_size(rvalue, 1000) reads it (see [parseSize]); the result is
 * divided by 8 and stored as a uint64 with no further bound.
 */
class ConfigParseCakeBandwidthOptionValue : SimpleGrammarOptionValues(
  "config_parse_cake_bandwidth",
  SequenceCombinator(parseSize(1000), EOF()),
)
