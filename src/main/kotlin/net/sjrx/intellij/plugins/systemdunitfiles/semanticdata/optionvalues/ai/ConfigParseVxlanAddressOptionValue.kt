package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import com.intellij.codeInspection.ProblemsHolder
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Combinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV4_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV6_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NetdevLinkCheckedSpan
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.netdevLinkIsMulticast

/*
 * [VXLAN] Group= / Local= / Remote= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#Local=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/vxlan.c#L220-L297   config_parse_vxlan_address (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/netdev-util.c#L9-L16   netdev_local_address_type_table
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L531-L551   in_addr_from_string_auto
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L103-L113   in_addr_is_multicast
 * linux  https://man7.org/linux/man-pages/man3/inet_pton.3.html
 */

private const val VXLAN_VALIDATOR = "config_parse_vxlan_address"

private val VXLAN_LOCAL_TYPE = FlexibleLiteralChoiceTerminal("ipv4_link_local", "ipv6_link_local", "dhcp4", "dhcp6", "slaac", "dhcp_pd")

/** An address literal that is (or, with [multicast] false, is not) a multicast address. */
private fun vxlanAddress(multicast: Boolean): Combinator = AlternativeCombinator(
    NetdevLinkCheckedSpan(IPV6_ADDR, if (multicast) "multicast" else "not multicast") { netdevLinkIsMulticast(it) == multicast },
    NetdevLinkCheckedSpan(IPV4_ADDR, if (multicast) "multicast" else "not multicast") { netdevLinkIsMulticast(it) == multicast },
)

/** `Group=`: a multicast address. */
private val VXLAN_GROUP = GrammarOptionValue(VXLAN_VALIDATOR, SequenceCombinator(vxlanAddress(multicast = true), EOF()))

/** `Remote=`: a unicast address. */
private val VXLAN_REMOTE = GrammarOptionValue(VXLAN_VALIDATOR, SequenceCombinator(vxlanAddress(multicast = false), EOF()))

/** `Local=`: a local-address type name, or a unicast address. Addresses first, so the name terminal cannot take a prefix. */
private val VXLAN_LOCAL = GrammarOptionValue(VXLAN_VALIDATOR, SequenceCombinator(AlternativeCombinator(vxlanAddress(multicast = false), VXLAN_LOCAL_TYPE), EOF()))

/**
 * Validator for `[VXLAN] Group=`, `Local=` and `Remote=` (.netdev).
 *
 * C function: config_parse_vxlan_address, which branches on the key name. The value is an IPv4 or IPv6
 * literal (in_addr_from_string_auto → inet_pton). For `Local=` only, one of the
 * netdev_local_address_type_table names (`ipv4_link_local`, `ipv6_link_local`, `dhcp4`, `dhcp6`, `slaac`,
 * `dhcp_pd`) is accepted first, telling networkd to pick up the address that mechanism assigned. `Group=`
 * must be a multicast address; `Local=` and `Remote=` must not be.
 *
 * Validators are looked up by (parser, ltype), not by key, so the key-specific rules are applied by
 * choosing a per-key grammar in [generateProblemDescriptors]. [combinator] (used for colouring and
 * completion) stays the union of the three. All of this was confirmed against systemd's parser built at
 * 0f0682b773: `Group=dhcp4`, `Remote=slaac`, `Group=192.168.1.1`, `Remote=239.1.1.1` and
 * `Local=239.1.1.1` are each logged and ignored.
 */
class ConfigParseVxlanAddressOptionValue : SimpleGrammarOptionValues(
    VXLAN_VALIDATOR,
    SequenceCombinator(AlternativeCombinator(IPV6_ADDR, IPV4_ADDR, VXLAN_LOCAL_TYPE), EOF())
) {
    override fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
        when (property.key) {
            "Group" -> VXLAN_GROUP.generateProblemDescriptors(property, holder)
            "Remote" -> VXLAN_REMOTE.generateProblemDescriptors(property, holder)
            "Local" -> VXLAN_LOCAL.generateProblemDescriptors(property, holder)
            else -> super.generateProblemDescriptors(property, holder)
        }
    }
}
