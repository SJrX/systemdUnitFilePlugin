package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Checked
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.PARSE_SIZE_IEC
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.parseSizeIec
import java.math.BigInteger

/*
 * [Route] TCPAdvertisedMaximumSegmentSize= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#TCPAdvertisedMaximumSegmentSize=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route.c#L1967-L2038        config_parse_route_section (ROUTE_METRIC_ADVMSS)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-metric.c#L373-L419   config_parse_route_metric (RTAX_ADVMSS)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-metric.c#L221-L251   config_parse_route_metric_advmss
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L157-L283              parse_size (base 1024)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-route-static.network
 */

private val UINT32_MAX: BigInteger = BigInteger.valueOf(4_294_967_295L)

/**
 * Validator for `[Route] TCPAdvertisedMaximumSegmentSize=` (.network) — ROUTE_METRIC_ADVMSS, which
 * config_parse_route_metric dispatches to config_parse_route_metric_advmss.
 *
 * A parse_size(…, 1024) byte count (`1460`, `1K`, `1.5K`, …) that must be in 1…UINT32_MAX. The man page
 * says 1…4294967294; the C bound is `u == 0 || u > UINT32_MAX`, so 4294967295 is accepted here.
 */
class ConfigParseRouteMetricAdvMssOptionValue : SimpleGrammarOptionValues(
    "config_parse_route_section",
    SequenceCombinator(
        Checked(PARSE_SIZE_IEC) { text -> parseSizeIec(text)?.let { it.signum() > 0 && it <= UINT32_MAX } ?: false },
        EOF()
    )
)
