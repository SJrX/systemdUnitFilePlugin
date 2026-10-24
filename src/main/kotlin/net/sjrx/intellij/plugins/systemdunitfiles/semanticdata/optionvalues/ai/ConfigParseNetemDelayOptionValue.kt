package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [NetworkEmulator] DelaySec=, DelayJitterSec= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#DelaySec=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#DelayJitterSec=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/netem.c#L51-L110   config_parse_netem_delay (QDISC_KIND_NETEM)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1289   parse_sec
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287   parse_time
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1147-L1196   extract_multiplier
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-time-util.c#L21-L84   TEST(parse_sec)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-netem.network#L12-L13   DelaySec=50ms, DelayJitterSec=10ms
 */

/**
 * Validator for [NetworkEmulator] DelaySec=, DelayJitterSec=.
 *
 * C function: config_parse_netem_delay (QDISC_KIND_NETEM). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * A time span as parse_sec() reads it (see [parseSec]). No further bound in the parser.
 *
 * Lenient: a delay too large for the kernel's tick conversion only fails later, in
 * network_emulator_fill_message, and is not checked here.
 */
class ConfigParseNetemDelayOptionValue : SimpleGrammarOptionValues(
  "config_parse_netem_delay",
  SequenceCombinator(parseSec(), EOF()),
)
