package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.DOT
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IntegerTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Labeled
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Role
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Network] IPv4LLStartAddress= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#IPv4LLStartAddress=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-ipv4ll.c#L389-L429   config_parse_ipv4ll_address (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L517-L529       in_addr_from_string (inet_pton, AF_INET)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L49-L61         in4_addr_is_link_local_dynamic
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-dhcp-client-with-ipv4ll.network#L9
 * linux  https://man7.org/linux/man-pages/man3/inet_pton.3.html
 */

/**
 * Validator for `[Network] IPv4LLStartAddress=`: an IPv4 address usable for dynamic IPv4 link-local
 * assignment — inside 169.254.0.0/16 but outside its first and last /24 (RFC 3927 §2.1), i.e.
 * 169.254.1.0 … 169.254.254.255.
 *
 * Every octet matches any number so a wrong address still highlights as an address; the ranges are
 * then enforced per octet.
 */
class ConfigParseIpv4llAddressOptionValue : SimpleGrammarOptionValues(
    "config_parse_ipv4ll_address",
    SequenceCombinator(
        Labeled(
            Role.LITERAL,
            SequenceCombinator(
                IntegerTerminal(169, 170), DOT, IntegerTerminal(254, 255), DOT, IntegerTerminal(1, 255), DOT, IntegerTerminal(0, 256),
            ),
        ),
        EOF(),
    )
)
