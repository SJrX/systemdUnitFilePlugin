package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [FairQueueing] CEThresholdSec= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#CEThresholdSec=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/fq.c#L284-L342   config_parse_fq_usec (QDISC_KIND_FQ)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1289   parse_sec
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287   parse_time
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1147-L1196   extract_multiplier
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-time-util.c#L21-L84   TEST(parse_sec)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-fq.network#L20   CEThresholdSec=100ms
 */

/**
 * Validator for [FairQueueing] CEThresholdSec=.
 *
 * C function: config_parse_fq_usec (QDISC_KIND_FQ). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * A time span as parse_sec() reads it (see [parseSec]) that must not exceed UINT32_MAX microseconds
 * (about 71min 34.967s): above it the parser logs "Specified 'CEThresholdSec=' is too large, ignoring
 * assignment". In particular `infinity` (USEC_INFINITY) is rejected — leave the value empty to get the
 * "unset/infinity" default.
 */
class ConfigParseFqUsecOptionValue : SimpleGrammarOptionValues(
  "config_parse_fq_usec",
  SequenceCombinator(parseSec(UINT32_MAX_USEC), EOF()),
)
