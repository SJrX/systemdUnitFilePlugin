package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import java.math.BigInteger

/*
 * [CAKE] RTTSec= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#RTTSec=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/cake.c#L614-L670   config_parse_cake_rtt
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1291   parse_time / parse_sec
 */

/**
 * Validator for [CAKE] RTTSec= (.network).
 *
 * C Function: config_parse_cake_rtt(QDISC_KIND_CAKE). An empty assignment resets it and is handled before
 * this grammar runs. The value is read with parse_sec() and then must satisfy `0 < t <= UINT32_MAX`
 * microseconds (about 71.6 minutes). So `0`, `72min`, `2hour` and `infinity` (USEC_INFINITY) are refused
 * ("Invalid 'RTTSec='"), and `71min` and `4294967295us` are accepted (sd-check at 0f0682b773).
 */
class ConfigParseCakeRttOptionValue : SimpleGrammarOptionValues(
  "config_parse_cake_rtt",
  SequenceCombinator(
    Checked(parseSec(maxUsec = UINT32_MAX_USEC)) { text -> evaluateParseSec(text)?.let { it > BigInteger.ZERO } ?: true },
    EOF(),
  ),
)
