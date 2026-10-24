package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtouTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Route] InitialCongestionWindow= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#InitialCongestionWindow=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route.c#L1967-L2038        config_parse_route_section (ROUTE_METRIC_INITCWND)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-metric.c#L373-L419   config_parse_route_metric (RTAX_INITCWND)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-metric.c#L284-L313   config_parse_tcp_window
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478              safe_atou_full (base 0)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-route-static.network#L37
 */

/**
 * Validator for `[Route] InitialCongestionWindow=` (.network) — ROUTE_METRIC_INITCWND, which
 * config_parse_route_metric dispatches to config_parse_tcp_window: safe_atou32() (so `0x10`, `0o20`,
 * `020` are numbers too) and then 1…1023.
 *
 * (`InitialAdvertisedReceiveWindow=` uses the same leaf and is already covered by
 * ConfigParseRouteSectionInitRwndOptionValue.)
 */
class ConfigParseRouteMetricInitCwndOptionValue : SimpleGrammarOptionValues(
    "config_parse_route_section",
    SequenceCombinator(SafeAtouTerminal(1, 1024), EOF())
)
