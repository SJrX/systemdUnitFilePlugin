package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtouTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [IPv6AddressLabel] Label= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Label=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-address-label.c#L419-L461   config_parse_ipv6_address_label_section (IPV6_ADDRESS_LABEL)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-address-label.c#L386-L417   config_parse_ipv6_address_label
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478               safe_atou_full (base 0)
 */

/**
 * Validator for `[IPv6AddressLabel] Label=` (.network): safe_atou32() — decimal, `0x` hex, `0o`/leading
 * zero octal, `0b` binary — in 0…4294967294. 4294967295 (0xffffffff) is reserved and refused.
 */
class ConfigParseIpv6AddressLabelOptionValue : SimpleGrammarOptionValues(
    "config_parse_ipv6_address_label_section",
    SequenceCombinator(SafeAtouTerminal(0, 4_294_967_295L), EOF())
)
