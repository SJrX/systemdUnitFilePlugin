package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IFNAME_OR_IFINDEX
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [DHCPServer] UplinkInterface=, [DHCPPrefixDelegation] UplinkInterface=, [IPv6SendRA] UplinkInterface=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#UplinkInterface=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L1441-L1516  config_parse_uplink (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L112-L124                parse_ifindex
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L785-L830               ifname_valid_full (IFNAME_VALID_ALTERNATIVE)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-dhcp6pd-upstream.network#L15
 *        https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-dhcp-server-downstream.network#L13
 */

/**
 * Validator for the three `UplinkInterface=` settings, all parsed by config_parse_uplink.
 *
 * Accepts `:auto`, `:none` (DHCPServer / IPv6SendRA only), `:self` (DHCPPrefixDelegation only), a
 * positive interface index, or an alternative interface name (see [IFNAME_OR_IFINDEX]). Which of
 * `:none` / `:self` is legal depends on the *section*, which the (parser, ltype) registration can't see,
 * so both are accepted everywhere (lenient). The special values are compared with streq, so they are
 * case-sensitive.
 */
class ConfigParseUplinkOptionValue : SimpleGrammarOptionValues(
    "config_parse_uplink",
    AlternativeCombinator(
        SequenceCombinator(FlexibleLiteralChoiceTerminal(":auto", ":none", ":self"), EOF()),
        SequenceCombinator(IFNAME_OR_IFINDEX, EOF()),
    )
)
