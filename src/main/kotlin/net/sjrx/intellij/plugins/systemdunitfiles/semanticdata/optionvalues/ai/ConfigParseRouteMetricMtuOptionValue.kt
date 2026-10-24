package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Checked
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.PARSE_SIZE_IEC
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.parseSizeIec
import java.math.BigInteger

/*
 * [Route] MTUBytes= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#MTUBytes=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route.c#L1967-L2038        config_parse_route_section (ROUTE_METRIC_MTU)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-metric.c#L373-L419   config_parse_route_metric (RTAX_MTU)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L1737-L1766           config_parse_mtu (ltype 0 = AF_UNSPEC)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L126-L155              parse_mtu
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L157-L283              parse_size (base 1024)
 */

private val UINT32_MAX: BigInteger = BigInteger.valueOf(4_294_967_295L)

/**
 * Validator for `[Route] MTUBytes=` (.network) — ROUTE_METRIC_MTU, which config_parse_route_metric
 * dispatches to config_parse_mtu with ltype 0 (AF_UNSPEC, so no family minimum).
 *
 * parse_mtu() reads the value with parse_size(…, 1024) — a byte count with optional `K`/`M`/`G`/…
 * suffixes (base 1024), fractions and multi-part sums like `1K 512` — and refuses anything above
 * UINT32_MAX. With AF_UNSPEC there is no lower bound, so `0` is accepted.
 */
class ConfigParseRouteMetricMtuOptionValue : SimpleGrammarOptionValues(
    "config_parse_route_section",
    SequenceCombinator(Checked(PARSE_SIZE_IEC) { text -> parseSizeIec(text)?.let { it <= UINT32_MAX } ?: false }, EOF())
)
