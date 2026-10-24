package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [HierarchyTokenBucketClass] Rate=, CeilRate= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Rate=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#CeilRate=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/htb.c#L378-L436   config_parse_htb_class_rate (TCLASS_KIND_HTB)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L157-L283   parse_size
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L108-L176   TEST(parse_size_iec)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L178-L246   TEST(parse_size_si)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-htb-fifo.network#L19-L20   Rate=1M, CeilRate=0.5M
 */

/**
 * Validator for [HierarchyTokenBucketClass] Rate=, CeilRate=.
 *
 * C function: config_parse_htb_class_rate (TCLASS_KIND_HTB). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * A rate in bits per second as parse_size(rvalue, 1000) reads it (see [parseSize]): `1M`, `0.5M`,
 * `1G 500M`, `125000`. The result is divided by 8 and stored as a uint64; no further bound.
 */
class ConfigParseHtbClassRateOptionValue : SimpleGrammarOptionValues(
  "config_parse_htb_class_rate",
  SequenceCombinator(parseSize(1000), EOF()),
)
