package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LDH_DNS_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [DHCPServerStaticLease] Hostname= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Hostname=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-server-static-lease.c#L222-L262  config_parse_dhcp_static_lease_hostname (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/dns-domain.h#L38-L47                           dns_name_is_valid_ldh (DNS_LABEL_LDH|DNS_LABEL_NO_ESCAPES)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/dns-domain.c#L24-L150                          dns_label_unescape
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/hostname-util.c#L42-L48                         valid_ldh_char
 */

/**
 * Validator for `[DHCPServerStaticLease] Hostname=`: a DNS name made of LDH labels — letters, digits and
 * hyphens, no leading or trailing hyphen, 1…63 characters each — joined by dots, with an optional single
 * trailing dot; no escapes (see [LDH_DNS_NAME]). The 253-character total is not enforced.
 */
class ConfigParseDhcpStaticLeaseHostnameOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_static_lease_hostname",
    SequenceCombinator(LDH_DNS_NAME, EOF())
)
