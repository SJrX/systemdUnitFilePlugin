package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CIDR_SEPARATOR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV4_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoNumberTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [DHCPServer] ServerAddress= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#ServerAddress=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-server.c#L889-L932  config_parse_dhcp_server_address (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L799-L843           in_addr_prefix_from_string (AF_INET)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L779-L797           in_addr_parse_prefixlen (safe_atou8, <= 32)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L517-L529           in_addr_from_string (inet_pton)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-dhcp-server.network
 * linux  https://man7.org/linux/man-pages/man3/inet_pton.3.html
 */

/**
 * Validator for `[DHCPServer] ServerAddress=`: an IPv4 address with an optional `/PREFIXLEN` of 0…32
 * (read by safe_atou8, so base-0 spellings like `0x18` are legal), e.g. `192.168.5.1/24`.
 *
 * systemd then also refuses a localhost (127.0.0.0/8) or link-local (169.254.0.0/16) address; that
 * range check is not modelled (lenient).
 */
class ConfigParseDhcpServerAddressOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_server_address",
    SequenceCombinator(
        IPV4_ADDR,
        ZeroOrOne(SequenceCombinator(CIDR_SEPARATOR, SafeAtoNumberTerminal(0, 33))),
        EOF(),
    )
)
