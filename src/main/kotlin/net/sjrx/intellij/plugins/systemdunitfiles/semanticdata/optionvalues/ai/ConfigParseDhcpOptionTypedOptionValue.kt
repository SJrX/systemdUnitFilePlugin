package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.dhcpOptionTypedData

/*
 * [DHCPRelay] CircuitId= / RemoteId= / VirtualSubnetSelection= and the deprecated
 * [DHCPServer] RelayAgentCircuitId= / RelayAgentRemoteId= (.network; RemoteId= also in networkd.conf).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#CircuitId=
 *        https://www.freedesktop.org/software/systemd/man/latest/networkd.conf.html#RemoteId=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L944-L1060   config_parse_dhcp_option (true = check_length)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L1176-L1183  dhcp_option_data_type_table
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188               extract_first_word (":" separator, coalescing)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478                safe_atou_full (base 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L517-L529              in_addr_from_string (inet_pton)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/escape.c#L104-L296                    cunescape_one
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-dhcp-relay-downstream.network#L13-L14
 *        https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-agent-bridge.network#L12
 * linux  https://man7.org/linux/man-pages/man3/inet_pton.3.html
 */

/**
 * Validator for config_parse_dhcp_option with ltype `true` (the gperf writes `/* check_length= */ true`):
 * `[DHCPRelay] CircuitId=`, `RemoteId=`, `VirtualSubnetSelection=` and the deprecated
 * `[DHCPServer] RelayAgentCircuitId=` / `RelayAgentRemoteId=`.
 *
 * The value is `TYPE:DATA` — see [dhcpOptionTypedData]: `uint8`/`uint16`/`uint32` with a number in range
 * (base 0, so hex/octal are legal), `ipv4address`/`ipv6address` with a bare address, or `string` with any
 * C-escaped text. With check_length set, systemd additionally refuses a string longer than 255 bytes once
 * unescaped; that length limit is not modelled (lenient).
 */
class ConfigParseDhcpOptionTypedOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_option",
    SequenceCombinator(
        ZeroOrMore(COLON),
        dhcpOptionTypedData(),
        EOF(),
    )
)
