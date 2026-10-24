package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [DeficitRoundRobinSchedulerClass] QuantumBytes= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#QuantumBytes=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/drr.c#L45-L102   config_parse_drr_size (TCLASS_KIND_DRR)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L157-L283   parse_size
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L108-L176   TEST(parse_size_iec)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L178-L246   TEST(parse_size_si)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-drr.network#L16   QuantumBytes=2000
 */

/**
 * Validator for [DeficitRoundRobinSchedulerClass] QuantumBytes=.
 *
 * C function: config_parse_drr_size (TCLASS_KIND_DRR). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * A byte size as parse_size(rvalue, 1024) reads it (see [parseSize]) that must not exceed UINT32_MAX:
 * above it the parser logs "Invalid 'QuantumBytes=', ignoring assignment". So `4G` (4294967296) is
 * rejected and `4095M` is accepted.
 */
class ConfigParseDrrSizeOptionValue : SimpleGrammarOptionValues(
  "config_parse_drr_size",
  SequenceCombinator(parseSize(1024, maxInclusive = SIZE_UINT32_MAX), EOF()),
)
