package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [FairQueueingControlledDelay] TargetSec=, IntervalSec=, CEThresholdSec= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#TargetSec=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#IntervalSec=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#CEThresholdSec=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/fq-codel.c#L157-L220   config_parse_fq_codel_usec (QDISC_KIND_FQ_CODEL)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1289   parse_sec
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287   parse_time
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1147-L1196   extract_multiplier
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-time-util.c#L21-L83   TEST(parse_sec)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-fq_codel.network#L15-L19   TargetSec=10ms, IntervalSec=200ms, CEThresholdSec=100ms
 */

/**
 * Validator for [FairQueueingControlledDelay] TargetSec=, IntervalSec=, CEThresholdSec=.
 *
 * C function: config_parse_fq_codel_usec (QDISC_KIND_FQ_CODEL). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * A time span as parse_sec() reads it: `infinity`, or one or more `number[unit]` elements such as
 * `10ms`, `1min30s`, `5s 500ms`, `.5s`, `+3.1s` (see [parseSec]). There is no extra bound: the parsed
 * usec_t is stored as is (CEThresholdSec= additionally treats an empty value as USEC_INFINITY).
 *
 * Overflow is rejected exactly as parse_time() does (ERANGE): an integer part above LLONG_MAX, or a finite
 * total reaching USEC_INFINITY (e.g. `18446744073709551615us`).
 */
class ConfigParseFqCodelUsecOptionValue : SimpleGrammarOptionValues(
  "config_parse_fq_codel_usec",
  SequenceCombinator(parseSec(), EOF()),
)
