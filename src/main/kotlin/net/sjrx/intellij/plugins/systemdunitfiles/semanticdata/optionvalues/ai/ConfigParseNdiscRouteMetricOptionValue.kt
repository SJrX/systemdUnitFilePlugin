package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Checked
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtouTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.UINT32_LIMIT
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.safeAtouValue

/*
 * [IPv6AcceptRA] RouteMetric= and the legacy [DHCPv6] RouteMetric= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#RouteMetric=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L451-L507   config_parse_ndisc_route_metric (0, AF_INET6)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478            safe_atou_full (base 0)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/systemd-networkd-tests.py#L8124   (RouteMetric=100:200:300)
 */

private val METRIC = SafeAtouTerminal(0, UINT32_LIMIT)

// high < medium < low, compared on the parsed numbers.
// safe_atou32 skips leading whitespace, so `100: 200: 300` is fine.
private val NEXT_METRIC = SafeAtouTerminal(0, UINT32_LIMIT, allowLeadingWhitespace = true)

// extract_many_words(…, EXTRACT_DONT_COALESCE_SEPARATORS) leaves p at "" after a single trailing `:`,
// which passes the isempty(p) check, so `100:200:300:` is accepted too.
private val METRIC_TRIPLE = Checked(SequenceCombinator(METRIC, COLON, NEXT_METRIC, COLON, NEXT_METRIC, ZeroOrOne(COLON))) { text ->
  val v = text.removeSuffix(":").split(':').map { safeAtouValue(it) }
  v.size == 3 && v.all { it != null } && v[0]!! < v[1]!! && v[1]!! < v[2]!!
}

/**
 * Validator for config_parse_ndisc_route_metric: `[IPv6AcceptRA] RouteMetric=` (ltype 0) and the legacy
 * `[DHCPv6] RouteMetric=` alias (ltype AF_INET6; the function ignores its ltype).
 *
 * Either one safe_atou32() metric used for all three router preferences, or `HIGH:MEDIUM:LOW` — exactly
 * three safe_atou32() fields split on `:` — which systemd only accepts when strictly increasing
 * (HIGH < MEDIUM < LOW, e.g. the default `512:1024:2048`). An empty field, a fourth field or a
 * non-increasing triple is ignored with a warning and flagged here.
 */
class ConfigParseNdiscRouteMetricOptionValue : SimpleGrammarOptionValues(
    "config_parse_ndisc_route_metric",
    SequenceCombinator(AlternativeCombinator(METRIC_TRIPLE, METRIC), EOF())
)
