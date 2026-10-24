package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IP_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [L2TP] Remote= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#Remote=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/l2tp-tunnel.c#L571-L623   config_parse_l2tp_tunnel_remote_address (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L531-L551   in_addr_from_string_auto
 * linux  https://man7.org/linux/man-pages/man3/inet_pton.3.html
 */

/**
 * Validator for `[L2TP] Remote=` (.netdev).
 *
 * C function: config_parse_l2tp_tunnel_remote_address: an IPv4 or IPv6 literal (in_addr_from_string_auto
 * → inet_pton). Not modelled: systemd also refuses the null address (`0.0.0.0`, `::`) and an address
 * whose family differs from an earlier `Local=`; both depend on the parsed value or on other keys. An
 * invalid value is logged and ignored by systemd.
 */
class ConfigParseL2tpTunnelRemoteAddressOptionValue : SimpleGrammarOptionValues(
    "config_parse_l2tp_tunnel_remote_address",
    SequenceCombinator(IP_ADDR, EOF())
)
