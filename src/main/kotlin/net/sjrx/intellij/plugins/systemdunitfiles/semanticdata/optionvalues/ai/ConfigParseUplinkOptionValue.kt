package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IFNAME_OR_IFINDEX
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.KeyOrSectionDispatchingGrammarOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [DHCPServer] UplinkInterface=, [DHCPPrefixDelegation] UplinkInterface=, [IPv6SendRA] UplinkInterface=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#UplinkInterface=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L1441-L1516  config_parse_uplink (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L1463-L1475  section → accept_none (:none vs :self)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L112-L124                parse_ifindex
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L785-L830               ifname_valid_full (IFNAME_VALID_ALTERNATIVE)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-dhcp6pd-upstream.network#L15
 *        https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-dhcp-server-downstream.network#L13
 */

private fun uplink(vararg special: String) = AlternativeCombinator(
    SequenceCombinator(FlexibleLiteralChoiceTerminal(*special), EOF()),
    SequenceCombinator(IFNAME_OR_IFINDEX, EOF()),
)

/**
 * Validator for the three `UplinkInterface=` settings, all parsed by config_parse_uplink.
 *
 * Accepts `:auto`, a positive interface index, or an alternative interface name (see
 * [IFNAME_OR_IFINDEX]), plus one section-specific word: `:none` in [DHCPServer] and [IPv6SendRA], `:self`
 * in [DHCPPrefixDelegation] (the C sets `accept_none = false` for that section and its deprecated
 * [DHCPv6PrefixDelegation] alias, which the plugin does not recognise as a section). The validator
 * dispatches on the section of the property it checks. The special values are compared with streq, so
 * they are case-sensitive. A `:none` in [DHCPPrefixDelegation] then fails ifname_valid_full (`:` is not
 * a name character) and is ignored with a warning — flagged here.
 */
class ConfigParseUplinkOptionValue : KeyOrSectionDispatchingGrammarOptionValue(
    VALIDATOR_NAME,
    uplink(":auto", ":none", ":self"),
    { _, section -> if (section == "DHCPPrefixDelegation" || section == "DHCPv6PrefixDelegation") SELF else NONE },
) {
  private companion object {
    const val VALIDATOR_NAME = "config_parse_uplink"
    val NONE = SimpleGrammarOptionValues(VALIDATOR_NAME, uplink(":auto", ":none"))
    val SELF = SimpleGrammarOptionValues(VALIDATOR_NAME, uplink(":auto", ":self"))
  }
}
