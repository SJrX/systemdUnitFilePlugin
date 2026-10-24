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
 * [IPv6PREF64Prefix] LifetimeSec= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BIPv6PREF64Prefix%5D%20Section%20Options
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-radv.c#L1240-L1283   config_parse_pref64_prefix_lifetime (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287      parse_time (via parse_sec)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1147-L1196      extract_multiplier
 */

private val EIGHT_SECONDS: BigInteger = BigInteger.valueOf(8).multiply(USEC_PER_SEC)
private val SCALED_LIFETIME_LIMIT: BigInteger = BigInteger.valueOf(1L shl 13)

/**
 * Validator for `[IPv6PREF64Prefix] LifetimeSec=` (.network).
 *
 * The PREF64 option (RFC 8781) carries the lifetime as a 13-bit count of 8-second units, so
 * config_parse_pref64_prefix_lifetime ignores ("PREF64 lifetime is too long") `infinity` and any value
 * where DIV_ROUND_UP(usec, 8s) ≥ 8192 — i.e. it accepts parse_sec() values up to 8191 × 8 = 65528
 * seconds (18h 12min 8s). `0` is accepted.
 */
class ConfigParsePref64PrefixLifetimeOptionValue : SimpleGrammarOptionValues(
    "config_parse_pref64_prefix_lifetime",
    SequenceCombinator(timeSpan { it != USEC_INFINITY && divRoundUp(it, EIGHT_SECONDS) < SCALED_LIFETIME_LIMIT }, EOF())
)
