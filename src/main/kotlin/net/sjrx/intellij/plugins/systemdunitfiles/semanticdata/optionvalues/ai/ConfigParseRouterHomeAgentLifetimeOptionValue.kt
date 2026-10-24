package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NETWORK_ROUTE_ADDR_USEC_INFINITY
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.USEC_PER_SEC
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.timeSpan
import java.math.BigInteger

/*
 * [IPv6SendRA] HomeAgentLifetimeSec= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#HomeAgentLifetimeSec=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-radv.c#L1583-L1624   config_parse_router_home_agent_lifetime (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287      parse_time (via parse_sec)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.h#L106-L108        timestamp_is_set
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/libsystemd-network/radv-internal.h#L72   RADV_HOME_AGENT_MAX_LIFETIME_USEC
 */

private val RADV_HOME_AGENT_MAX_LIFETIME_USEC: BigInteger = BigInteger.valueOf(65_535L).multiply(USEC_PER_SEC)

/**
 * Validator for `[IPv6SendRA] HomeAgentLifetimeSec=` (.network).
 *
 * config_parse_router_home_agent_lifetime reads a time span with parse_sec() and ignores it unless
 * timestamp_is_set() (neither `0` nor `infinity`) and it is at most RADV_HOME_AGENT_MAX_LIFETIME_USEC =
 * 65535 seconds. Sub-second values such as `500ms` pass the C check (the man page's "1…65535" is in
 * whole seconds, but the comparison is on microseconds), so they are accepted here too.
 */
class ConfigParseRouterHomeAgentLifetimeOptionValue : SimpleGrammarOptionValues(
    "config_parse_router_home_agent_lifetime",
    SequenceCombinator(timeSpan { it.signum() > 0 && it != NETWORK_ROUTE_ADDR_USEC_INFINITY && it <= RADV_HOME_AGENT_MAX_LIFETIME_USEC }, EOF())
)
