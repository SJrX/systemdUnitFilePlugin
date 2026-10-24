package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NETWORK_DHCP_MISC_COALESCED_COLONS
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.KeyOrSectionDispatchingGrammarOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NetworkDhcpMiscSafeAtoNumberTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.dhcpOptionTypedData

/*
 * [DHCPv6] SendOption= / SendVendorOption= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#SendOption=
 *        https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#SendVendorOption=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L745-L942    config_parse_dhcp6_send_option (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L781         streq(lvalue, "SendVendorOption") — the key decides the shape
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L1176-L1183  dhcp_option_data_type_table
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188               extract_first_word (":" separator, coalescing)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L599-L613                safe_atou16_full (base 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478                safe_atou_full (safe_atou32, base 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L517-L529              in_addr_from_string (inet_pton)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/escape.c#L104-L296                    cunescape_one
 * linux  https://man7.org/linux/man-pages/man3/inet_pton.3.html
 */

private val DHCP6_OPTION_CODE = NetworkDhcpMiscSafeAtoNumberTerminal(1, 65_535L)

// safe_atou16/safe_atou32 skip leading whitespace, and extract_first_word(…, ":", 0) leaves any that
// follows a colon inside the next word, so `123: 1:string:x` is fine.
private val OPTIONAL_WS = ZeroOrOne(WhitespaceTerminal())

private val CODE_TYPE_DATA = SequenceCombinator(OPTIONAL_WS, DHCP6_OPTION_CODE, NETWORK_DHCP_MISC_COALESCED_COLONS, dhcpOptionTypedData(), EOF())

private val SEND_OPTION = SequenceCombinator(ZeroOrMore(COLON), CODE_TYPE_DATA)

private val SEND_VENDOR_OPTION = SequenceCombinator(
    ZeroOrMore(COLON),
    OPTIONAL_WS,
    NetworkDhcpMiscSafeAtoNumberTerminal(0, 4_294_967_296L),
    NETWORK_DHCP_MISC_COALESCED_COLONS,
    CODE_TYPE_DATA,
)

/**
 * Validator for `[DHCPv6] SendOption=` (`CODE:TYPE:DATA`) and `[DHCPv6] SendVendorOption=`
 * (`ENTERPRISE:CODE:TYPE:DATA`), both parsed by config_parse_dhcp6_send_option.
 *
 * The parser branches on the *key name*: only for SendVendorOption= does it first read an enterprise
 * identifier (safe_atou32, any uint32 — the man page says 1…4294967294 but C accepts 0 and 4294967295
 * too). The validator is registered per (parser, ltype) but dispatches on the key it is checking, so
 * `SendOption=` takes exactly the three-field form and `SendVendorOption=` exactly the four-field one
 * (`SendVendorOption=12:string:x` is refused, as systemd's safe_atou16("string") refuses it).
 *
 * CODE is safe_atou16 in 1…65534 — the C check is `u16 < 1 || u16 >= UINT16_MAX`, so 65535 is refused
 * although the man page says "1…65536". The TYPE:DATA tail is [dhcpOptionTypedData]; all six types are
 * accepted. Every separator coalesces, so leading and doubled colons are harmless.
 */
class ConfigParseDhcp6SendOptionOptionValue : KeyOrSectionDispatchingGrammarOptionValue(
    VALIDATOR_NAME,
    AlternativeCombinator(SEND_VENDOR_OPTION, SEND_OPTION),
    { key, _ -> if (key == "SendVendorOption") VENDOR else PLAIN },
) {
  private companion object {
    const val VALIDATOR_NAME = "config_parse_dhcp6_send_option"
    val PLAIN = SimpleGrammarOptionValues(VALIDATOR_NAME, SEND_OPTION)
    val VENDOR = SimpleGrammarOptionValues(VALIDATOR_NAME, SEND_VENDOR_OPTION)
  }
}
