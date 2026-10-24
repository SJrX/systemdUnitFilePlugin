package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NetworkDhcpMiscCheckedSpan
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.networkDhcpMiscParseSecUsec
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.PARSE_SEC_TIMESPAN

/*
 * [DHCPServer] IPv6OnlyPreferredSec= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#IPv6OnlyPreferredSec=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-server.c#L934-L974  config_parse_dhcp_server_ipv6_only_preferred (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-server.c#L966-L970  t < MIN_V6ONLY_WAIT_USEC && !network_test_mode_enabled()
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/libsystemd-network/dhcp-protocol.h#L15     MIN_V6ONLY_WAIT_USEC (300 s)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1289-L1291            parse_sec
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287          parse_time
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-time-util.c#L21-L82        TEST(parse_sec)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-dhcp-server-ipv6-only-mode.network#L16
 */

/**
 * Validator for `[DHCPServer] IPv6OnlyPreferredSec=`: a time span as parse_sec() reads it (`30min`,
 * `1h30min`, `.5h`, `infinity`) — see [PARSE_SEC_TIMESPAN].
 *
 * The duration must also be at least MIN_V6ONLY_WAIT_USEC, 300 s (`5min`, `300`, `4min 60s`); anything
 * shorter, `0` included, is ignored with "Invalid [DHCPServer] IPv6OnlyPreferredSec=" (checked with
 * systemd's parser at 0f0682b773). The only exception is networkd's test mode
 * (`SYSTEMD_NETWORK_TEST_MODE`), which systemd's own test suite uses for its `20s` fixture; that is not a
 * production setting, so short values are flagged.
 */
class ConfigParseDhcpServerIpv6OnlyPreferredOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_server_ipv6_only_preferred",
    SequenceCombinator(
        NetworkDhcpMiscCheckedSpan(PARSE_SEC_TIMESPAN, "AtLeast300s") { (networkDhcpMiscParseSecUsec(it) ?: Long.MAX_VALUE) >= 300_000_000L },
        EOF(),
    )
)
