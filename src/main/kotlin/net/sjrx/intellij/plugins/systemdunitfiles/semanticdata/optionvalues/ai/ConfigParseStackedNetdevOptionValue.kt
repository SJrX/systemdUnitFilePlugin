package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Checked
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.INTERFACE_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.plainNameList

/*
 * [Network] IPoIB= / IPVLAN= / IPVTAP= / MACsec= / MACVLAN= / MACVTAP= / Tunnel= / VLAN= / VXLAN= /
 * Xfrm= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#IPoIB=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-network.c#L945-L1008   config_parse_stacked_netdev (NETDEV_KIND_IPOIB, …_IPVLAN, …_IPVTAP, …_MACSEC, …_MACVLAN, …_MACVTAP, …_VLAN, …_VXLAN, …_XFRM, _NETDEV_KIND_TUNNEL)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188       extract_first_word (flags 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L785-L830       ifname_valid_full (flags 0, via ifname_valid)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L112-L124        parse_ifindex (safe_atoi base 0, > 0)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-macvlan.network#L7   (MACVLAN=macvlan99)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-ipvlan.network#L7    (IPVLAN=ipvlan99)
 */

// ifname_valid() first refuses anything parse_ifindex() accepts: safe_atoi() with base 0 (so hex, `0b`,
// `0o` and a leading `+` count) giving a value in 1…INT_MAX. INTERFACE_NAME only refuses all-digit names.
// The name is a whole word handed to safe_atoi, so leading whitespace that survived the split (`\v`) is
// skipped as C does.
private val NOT_AN_IFINDEX_NAME = Checked(INTERFACE_NAME) { name ->
  SafeAtoTerminal(
    signed = true,
    base = 0,
    flags = emptySet(),
    minInclusive = 1L,
    maxInclusive = Int.MAX_VALUE.toLong(),
    acceptsWhitespace = true,
    acceptsMinus = true,
  ).valueOf(name) == null
}

/**
 * Validator for the stacked-netdev settings of a .network `[Network]` section: `IPoIB=`, `IPVLAN=`,
 * `IPVTAP=`, `MACsec=`, `MACVLAN=`, `MACVTAP=`, `Tunnel=`, `VLAN=`, `VXLAN=` and `Xfrm=`. All ten use
 * config_parse_stacked_netdev; the ltype only records which netdev kind is expected, so they share a
 * grammar.
 *
 * The value is a whitespace-separated list (extract_first_word with no flags: no quote handling) of
 * netdev names, each checked with ifname_valid(): 1–15 printable ASCII characters other than `:`, `/`
 * and `%`, not all digits, not something parse_ifindex() reads as a positive ifindex (`0x10`, `+5`,
 * `0b1`), and not `.`, `..`, `all` or `default`. An invalid name is logged and skipped;
 * it is flagged here. An unmatched backslash at the very end of the value would also make
 * extract_first_word fail, but is not singled out.
 */
class ConfigParseStackedNetdevOptionValue : SimpleGrammarOptionValues(
    "config_parse_stacked_netdev",
    plainNameList(NOT_AN_IFINDEX_NAME),
)
