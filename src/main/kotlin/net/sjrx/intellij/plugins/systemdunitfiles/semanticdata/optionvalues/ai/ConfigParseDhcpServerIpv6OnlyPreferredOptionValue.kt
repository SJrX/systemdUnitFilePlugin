package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.PARSE_SEC_TIMESPAN

/*
 * [DHCPServer] IPv6OnlyPreferredSec= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#IPv6OnlyPreferredSec=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-server.c#L934-L974  config_parse_dhcp_server_ipv6_only_preferred (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1289-L1291            parse_sec
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287          parse_time
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-time-util.c#L21-L82        TEST(parse_sec)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-dhcp-server-ipv6-only-mode.network#L16
 */

/**
 * Validator for `[DHCPServer] IPv6OnlyPreferredSec=`: a time span as parse_sec() reads it (`30min`,
 * `1h30min`, `.5h`, `infinity`) — see [PARSE_SEC_TIMESPAN].
 *
 * systemd also refuses anything below MIN_V6ONLY_WAIT_USEC (300s) unless networkd runs in its test
 * mode — systemd's own test suite uses `20s`. That magnitude check depends on the parsed duration and on
 * a runtime flag, so it is not modelled (lenient).
 */
class ConfigParseDhcpServerIpv6OnlyPreferredOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_server_ipv6_only_preferred",
    SequenceCombinator(PARSE_SEC_TIMESPAN, EOF())
)
