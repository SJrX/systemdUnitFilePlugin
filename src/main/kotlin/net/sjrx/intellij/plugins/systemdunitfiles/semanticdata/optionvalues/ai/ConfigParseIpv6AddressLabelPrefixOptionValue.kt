package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Checked
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV6_PREFIX_SAFE_LEN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ipLiteralBytes
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.safeAtouValue

/*
 * [IPv6AddressLabel] Prefix= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BIPv6AddressLabel%5D%20Section%20Options
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-address-label.c#L419-L461   config_parse_ipv6_address_label_section (IPV6_ADDRESS_LABEL_PREFIX)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-address-label.c#L346-L384   config_parse_ipv6_address_label_prefix
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L799-L843             in_addr_prefix_from_string (AF_INET6)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L165-L171             in6_addr_is_ipv4_mapped_address
 * linux  https://man7.org/linux/man-pages/man3/inet_pton.3.html   (kernel net/ipv6/addrlabel.c ip6addrlbl_alloc)
 */

/** True for an IPv4-mapped address (`::ffff:a.b.c.d`) whose prefix length exceeds 96. */
private fun isMappedTooLong(text: String): Boolean {
  val slash = text.indexOf('/')
  val bytes = ipLiteralBytes(if (slash >= 0) text.substring(0, slash) else text) ?: return false
  val mapped = (0 until 10).all { bytes[it].toInt() == 0 } && bytes[10].toInt() == -1 && bytes[11].toInt() == -1
  val length = if (slash >= 0) safeAtouValue(text.substring(slash + 1))?.toInt() ?: return false else 128
  return mapped && length > 96
}

/**
 * Validator for `[IPv6AddressLabel] Prefix=` (.network).
 *
 * An IPv6 prefix as in_addr_prefix_from_string(AF_INET6) reads it — an IPv6 literal with an optional
 * `/LEN` in 0…128 (no slash means /128). config_parse_ipv6_address_label_prefix then refuses an
 * IPv4-mapped address (`::ffff:0:0/96`) with a prefix length above 96, mirroring the kernel's
 * ip6addrlbl_alloc(); note that this includes an IPv4-mapped address written without a slash.
 */
class ConfigParseIpv6AddressLabelPrefixOptionValue : SimpleGrammarOptionValues(
    "config_parse_ipv6_address_label_section",
    SequenceCombinator(Checked(IPV6_PREFIX_SAFE_LEN) { !isMappedTooLong(it) }, EOF())
)
