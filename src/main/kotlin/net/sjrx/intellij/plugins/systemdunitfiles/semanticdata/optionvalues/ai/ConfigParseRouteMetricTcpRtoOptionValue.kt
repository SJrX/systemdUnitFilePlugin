package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NETWORK_ROUTE_ADDR_USEC_INFINITY
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.USEC_PER_MSEC
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.divRoundUp
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.timeSpan
import java.math.BigInteger

/*
 * [Route] TCPRetransmissionTimeoutSec= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#TCPRetransmissionTimeoutSec=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route.c#L1967-L2038          config_parse_route_section (ROUTE_METRIC_RTO_MIN)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-metric.c#L373-L419     config_parse_route_metric (RTAX_RTO_MIN)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-metric.c#L315-L346     config_parse_route_metric_tcp_rto
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287               parse_time (via parse_sec)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.h#L106-L108                 timestamp_is_set
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-route-congctl.network
 */

private val UINT32_MAX: BigInteger = BigInteger.valueOf(4_294_967_295L)

/**
 * Validator for `[Route] TCPRetransmissionTimeoutSec=` (.network) — ROUTE_METRIC_RTO_MIN, which
 * config_parse_route_metric dispatches to config_parse_route_metric_tcp_rto.
 *
 * A parse_sec() time span that must be set (neither `0` nor `infinity`) and must round up to at most
 * UINT32_MAX milliseconds, since RTAX_RTO_MIN is a u32 of milliseconds.
 */
class ConfigParseRouteMetricTcpRtoOptionValue : SimpleGrammarOptionValues(
    "config_parse_route_section",
    SequenceCombinator(
        timeSpan { it.signum() > 0 && it != NETWORK_ROUTE_ADDR_USEC_INFINITY && divRoundUp(it, USEC_PER_MSEC) <= UINT32_MAX },
        EOF()
    )
)
