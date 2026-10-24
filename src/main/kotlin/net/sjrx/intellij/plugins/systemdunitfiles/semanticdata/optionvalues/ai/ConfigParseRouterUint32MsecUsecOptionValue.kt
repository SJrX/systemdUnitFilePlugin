package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NETWORK_ROUTE_ADDR_USEC_INFINITY
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.USEC_PER_MSEC
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.timeSpan
import java.math.BigInteger

/*
 * [IPv6SendRA] ReachableTimeSec= / RetransmitSec= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#ReachableTimeSec=
 *        https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#RetransmitSec=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-radv.c#L1514-L1555   config_parse_router_uint32_msec_usec (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287      parse_time (via parse_sec)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1147-L1196      extract_multiplier
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/libsystemd-network/radv-internal.h#L44   RADV_MAX_UINT32_MSEC_USEC
 */

private val RADV_MAX_UINT32_MSEC_USEC: BigInteger = BigInteger.valueOf(4_294_967_295L).multiply(USEC_PER_MSEC)

/**
 * Validator for `[IPv6SendRA] ReachableTimeSec=` and `RetransmitSec=` (.network).
 *
 * config_parse_router_uint32_msec_usec reads a time span with parse_sec() (default unit seconds, every
 * suffix extract_multiplier() knows, `infinity`) and ignores it unless it is `infinity` or at most
 * RADV_MAX_UINT32_MSEC_USEC = UINT32_MAX milliseconds (~49.7 days), since the value is sent as a 32-bit
 * millisecond count. The man page documents only the 0…4294967295 ms range; the C also lets `infinity`
 * through, so it is accepted here.
 */
class ConfigParseRouterUint32MsecUsecOptionValue : SimpleGrammarOptionValues(
    "config_parse_router_uint32_msec_usec",
    SequenceCombinator(timeSpan { it == NETWORK_ROUTE_ADDR_USEC_INFINITY || it <= RADV_MAX_UINT32_MSEC_USEC }, EOF())
)
