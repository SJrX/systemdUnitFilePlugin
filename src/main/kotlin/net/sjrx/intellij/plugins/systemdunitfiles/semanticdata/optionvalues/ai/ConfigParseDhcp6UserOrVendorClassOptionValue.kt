package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NETWORK_DHCP_MISC_NONEMPTY_UNQUOTE_WORD
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.wordList

/*
 * [DHCPv6] UserClass= / VendorClass= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#UserClass=
 *        https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#VendorClass=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L700-L743  config_parse_dhcp6_user_or_vendor_class (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188            extract_first_word (EXTRACT_CUNESCAPE|EXTRACT_UNQUOTE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/escape.c#L104-L296                 cunescape_one
 */

/**
 * Validator for `[DHCPv6] UserClass=` and `[DHCPv6] VendorClass=`: the same quoted/escaped word list as
 * [ConfigParseDhcp4UserClassOptionValue], with a 1…65535-byte length limit per entry: empty
 * entries are flagged, the upper bound is not modelled.
 */
class ConfigParseDhcp6UserOrVendorClassOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp6_user_or_vendor_class",
    SequenceCombinator(wordList(NETWORK_DHCP_MISC_NONEMPTY_UNQUOTE_WORD), EOF())
)
