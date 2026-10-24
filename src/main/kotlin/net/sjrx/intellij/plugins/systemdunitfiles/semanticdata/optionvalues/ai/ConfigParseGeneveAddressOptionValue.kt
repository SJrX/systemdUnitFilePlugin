package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IP_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NetdevLinkCheckedSpan
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.netdevLinkIsMulticast

/*
 * [GENEVE] Remote= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#Remote=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/geneve.c#L124-L163   config_parse_geneve_address (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L531-L551   in_addr_from_string_auto
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L103-L113   in_addr_is_multicast
 * linux  https://man7.org/linux/man-pages/man3/inet_pton.3.html
 */

/**
 * Validator for `[GENEVE] Remote=` (.netdev).
 *
 * C function: config_parse_geneve_address: an IPv4 or IPv6 literal (in_addr_from_string_auto →
 * inet_pton) that is not a multicast address (224.0.0.0/4, ff00::/8). An invalid value is logged and
 * ignored by systemd.
 */
class ConfigParseGeneveAddressOptionValue : SimpleGrammarOptionValues(
    "config_parse_geneve_address",
    SequenceCombinator(NetdevLinkCheckedSpan(IP_ADDR, "not multicast") { !netdevLinkIsMulticast(it) }, EOF())
)
