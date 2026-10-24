package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [NetworkEmulator] LossRate=, DuplicateRate= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#LossRate=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#DuplicateRate=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/netem.c#L112-L170   config_parse_netem_rate (QDISC_KIND_NETEM)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/tc-util.c#L63-L75   parse_tc_percent
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/percent-util.c#L135-L157   parse_permyriad / parse_permyriad_unbounded
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/percent-util.c#L57-L99   parse_parts_value_with_hundredths_place
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L497-L519   safe_atoi (base 0)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-percent-util.c#L87-L131   TEST(parse_permyriad)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-netem.network#L15   LossRate=20%
 */

/**
 * Validator for [NetworkEmulator] LossRate=, DuplicateRate=.
 *
 * C function: config_parse_netem_rate (QDISC_KIND_NETEM). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * A percentage as parse_tc_percent() -> parse_permyriad() reads it: an integer followed by `%` (up to
 * two decimals), `‰` (up to one decimal) or `‱` (no decimals), at most 100% (see [TC_PERCENT]). A
 * bare number without a symbol is rejected. The integer part goes through safe_atoi (base 0), so
 * `0x10%` is legal; that corner is accepted.
 */
class ConfigParseNetemRateOptionValue : SimpleGrammarOptionValues(
  "config_parse_netem_rate",
  SequenceCombinator(TC_PERCENT, EOF()),
)
