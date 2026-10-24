package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.USEC_INFINITY
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.USEC_PER_SEC
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.divRoundUp
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.timeSpan
import java.math.BigInteger

/*
 * [IPv6RoutePrefix] LifetimeSec= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BIPv6RoutePrefix%5D%20Section%20Options
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-radv.c#L1116-L1159   config_parse_route_prefix_lifetime (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287      parse_time (via parse_sec)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1147-L1196      extract_multiplier
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-ipv6ra-prefix.network#L24-L26
 */

private val UINT32_MAX: BigInteger = BigInteger.valueOf(4_294_967_295L)

/**
 * Validator for `[IPv6RoutePrefix] LifetimeSec=` (.network).
 *
 * config_parse_route_prefix_lifetime reads a time span with parse_sec() and ignores it ("Lifetime is
 * too long") unless it is `infinity` or rounds up to fewer than UINT32_MAX whole seconds — the Route
 * Information option carries a 32-bit seconds field in which all-ones means infinity.
 */
class ConfigParseRoutePrefixLifetimeOptionValue : SimpleGrammarOptionValues(
    "config_parse_route_prefix_lifetime",
    SequenceCombinator(timeSpan { it == USEC_INFINITY || divRoundUp(it, USEC_PER_SEC) < UINT32_MAX }, EOF())
)
