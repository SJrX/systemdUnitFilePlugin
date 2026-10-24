package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Checked
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IP_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ipIsMulticast
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ipIsNull
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ipLiteralBytes

/*
 * [Network] ProxyNeighbor= and its deprecated alias IPv6ProxyNDPAddress= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#ProxyNeighbor=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-neighbor-proxy.c#L157-L216   config_parse_neighbor_proxy_address (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L531-L551   in_addr_from_string_auto
 * linux  https://man7.org/linux/man-pages/man3/inet_pton.3.html
 */

/**
 * Validator for `[Network] ProxyNeighbor=` and the deprecated `IPv6ProxyNDPAddress=` (.network).
 *
 * config_parse_neighbor_proxy_address reads one address of either family with in_addr_from_string_auto
 * (inet_pton(AF_INET) then inet_pton(AF_INET6) — no prefix length, no zone index) and then ignores it
 * with a warning if it is
 *  - the ANY address (`0.0.0.0`, `::`),
 *  - multicast (224.0.0.0/4 or ff00::/8), or
 *  - the IPv4 limited broadcast address `255.255.255.255`,
 * because the kernel would refuse such a proxy entry. All three are flagged here.
 */
class ConfigParseNeighborProxyAddressOptionValue : SimpleGrammarOptionValues(
    "config_parse_neighbor_proxy_address",
    SequenceCombinator(
        Checked(IP_ADDR) { text ->
          val bytes = ipLiteralBytes(text)
          bytes != null && !ipIsNull(bytes) && !ipIsMulticast(bytes) && !(bytes.size == 4 && bytes.all { it.toInt() == -1 })
        },
        EOF()
    )
)
