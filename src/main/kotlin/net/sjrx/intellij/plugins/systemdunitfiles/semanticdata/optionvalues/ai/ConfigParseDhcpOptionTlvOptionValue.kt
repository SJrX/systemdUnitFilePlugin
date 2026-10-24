package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NETWORK_DHCP_MISC_COALESCED_COLONS
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.dhcpOptionTypedData

/*
 * [DHCPv4] SendOption= / SendVendorOption=, [DHCPServer] SendOption= / SendVendorOption=,
 * [DHCPRelay] ExtraOption= (.network and networkd.conf).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#SendOption=
 *        https://www.freedesktop.org/software/systemd/man/latest/networkd.conf.html#ExtraOption=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L1062-L1108   config_parse_dhcp_option_tlv (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L944-L1060    config_parse_dhcp_option (TYPE:DATA tail, check_length=false)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L1176-L1183   dhcp_option_data_type_table
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188                extract_first_word (":" separator, coalescing)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L583-L597                 safe_atou8_full (base 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L517-L529               in_addr_from_string (inet_pton)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/escape.c#L104-L296                     cunescape_one
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-dhcp-client-ipv4-only.network#L21
 * linux  https://man7.org/linux/man-pages/man3/inet_pton.3.html
 */

/**
 * Validator for the `CODE:TYPE:DATA` raw-option settings parsed by config_parse_dhcp_option_tlv:
 * `[DHCPv4] SendOption=`, `[DHCPv4] SendVendorOption=`, `[DHCPServer] SendOption=`,
 * `[DHCPServer] SendVendorOption=` and `[DHCPRelay] ExtraOption=` (in both .network files and
 * networkd.conf).
 *
 * CODE is read by safe_atou8 (so `0x0c` and `014` are 12 too, and leading whitespace, `0b 1` or `\v5` are
 * fine because the field is cut on `:`, not on whitespace) and must be 1…254. The rest is handed to
 * config_parse_dhcp_option and has the [dhcpOptionTypedData] shape: `uint8`/`uint16`/`uint32` take a
 * number in range, `ipv4address`/`ipv6address` a bare address, `string` any C-escaped text (which may
 * itself contain colons). Every separator is extract_first_word(…, ":", 0), so leading and repeated
 * colons are coalesced and accepted.
 *
 * The man page lists only uint8/uint16/uint32/ipv4address/string for [DHCPv4]; the C parser shares
 * the full type table, so `ipv6address` is accepted everywhere (C wins).
 */
class ConfigParseDhcpOptionTlvOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_option_tlv",
    SequenceCombinator(
        ZeroOrMore(COLON),
        SafeAtoTerminal(
            signed = false,
            base = 0,
            flags = emptySet(),
            minInclusive = 1L,
            maxInclusive = 254L,
            acceptsWhitespace = true,
            acceptsMinus = true,
        ),
        NETWORK_DHCP_MISC_COALESCED_COLONS,
        dhcpOptionTypedData(),
        EOF(),
    )
)
