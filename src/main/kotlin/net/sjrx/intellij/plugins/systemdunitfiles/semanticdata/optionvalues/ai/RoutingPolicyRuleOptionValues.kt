package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Combinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.HYPHEN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.INTERFACE_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IP_ADDR_AND_ANY_PREFIX
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.unsignedNumber
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * The [RoutingPolicyRule] section of a .network file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BRoutingPolicyRule%5D%20Section%20Options
 * parser https://github.com/systemd/systemd/blob/a8e93919c3/src/network/networkd-routing-policy-rule.c  config_parse_routing_policy_rule
 * keys   systemd-build/build/networkd-network-gperf.gperf
 *
 * config_parse_routing_policy_rule is a dispatcher: the ltype indexes a ConfigSectionParser table and
 * each entry names the parser that actually reads the value, so this file mirrors the table entry by
 * entry rather than giving the whole section one grammar.
 */

/*
 * Validators for the `[RoutingPolicyRule]` section of a .network file (#509).
 *
 * `config_parse_routing_policy_rule` (src/network/networkd-routing-policy-rule.c) is a dispatcher:
 * its ltype selects an entry in a ConfigSectionParser table, and each entry names the parser that
 * actually reads the value. The classes below mirror that table entry by entry, so each one is
 * registered under the matching ROUTING_POLICY_RULE_* ltype.
 *
 * Not covered here: ROUTING_POLICY_RULE_TABLE (route table names are user-defined via RouteTable= in
 * networkd.conf, so there is no closed set to check), ROUTING_POLICY_RULE_FWMARK and
 * ROUTING_POLICY_RULE_IP_PROTOCOL.
 */

private const val RULE = "config_parse_routing_policy_rule"

/**
 * A `n` or `n-m` range — systemd's parse_range(), which reads each bound with safe_atou() and so
 * accepts the same decimal / hexadecimal / octal spellings [unsignedNumber] does.
 */
private fun range(minInclusive: Long, maxExclusive: Long): Combinator {
  val bound = unsignedNumber(maxExclusive, minInclusive)
  return SequenceCombinator(bound, ZeroOrOne(SequenceCombinator(HYPHEN, bound)), EOF())
}

/**
 * `From=` / `To=` — table entry config_parse_in_addr_prefix, i.e.
 * in_addr_prefix_from_string_auto_full with PREFIXLEN_FULL: an address of either family with an
 * optional prefix length, which defaults to the family's full width when omitted.
 */
class ConfigParseRoutingPolicyRuleFromToOptionValue : SimpleGrammarOptionValues(
    RULE, SequenceCombinator(IP_ADDR_AND_ANY_PREFIX, EOF())
)

/**
 * `IncomingInterface=` / `OutgoingInterface=` — table entry config_parse_ifname, i.e.
 * ifname_valid() with no flags.
 */
class ConfigParseRoutingPolicyRuleInterfaceOptionValue : SimpleGrammarOptionValues(
    RULE, SequenceCombinator(INTERFACE_NAME, EOF())
)

/** `Priority=` — table entry config_parse_routing_policy_rule_priority: safe_atou32. */
class ConfigParseRoutingPolicyRulePriorityOptionValue : SimpleGrammarOptionValues(
    RULE, SequenceCombinator(unsignedNumber(4_294_967_296L), EOF())
)

/**
 * `GoTo=` — table entry config_parse_routing_policy_rule_goto: safe_atou32, and then rejected unless
 * it is greater than zero.
 */
class ConfigParseRoutingPolicyRuleGotoOptionValue : SimpleGrammarOptionValues(
    RULE, SequenceCombinator(unsignedNumber(4_294_967_296L, minInclusive = 1), EOF())
)

/** `TypeOfService=` — table entry config_parse_uint8. */
class ConfigParseRoutingPolicyRuleTosOptionValue : SimpleGrammarOptionValues(
    RULE, SequenceCombinator(unsignedNumber(256), EOF())
)

/**
 * `L3MasterDevice=` — table entry config_parse_bool — and `Invert=`, whose entry is
 * config_parse_uint32_flag: both read the value with parse_boolean() and only differ in where the
 * result is stored.
 */
class ConfigParseRoutingPolicyRuleBooleanOptionValue : SimpleGrammarOptionValues(
    RULE, SequenceCombinator(BOOLEAN, EOF())
)

/**
 * `Family=` — table entry config_parse_routing_policy_rule_family, which resolves the value through
 * routing_policy_rule_address_family_table (src/network/networkd-util.c). Note this table spells
 * "both" where the plain address_family_table would say "yes".
 */
class ConfigParseRoutingPolicyRuleFamilyOptionValue : SimpleGrammarOptionValues(
    RULE, SequenceCombinator(FlexibleLiteralChoiceTerminal("both", "ipv4", "ipv6"), EOF())
)

/**
 * `Type=` — table entry config_parse_routing_policy_rule_action, resolved through fr_act_type_table.
 */
class ConfigParseRoutingPolicyRuleActionOptionValue : SimpleGrammarOptionValues(
    RULE,
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("table", "goto", "nop", "blackhole", "unreachable", "prohibit"),
        EOF()
    )
)

/**
 * `SourcePort=` / `DestinationPort=` — table entry config_parse_routing_policy_rule_port_range, i.e.
 * parse_ip_port_range with allow_zero = false: a port or a `low-high` range, each 1…65535.
 *
 * systemd additionally rejects a range whose high end is below its low end. That compares the two
 * parsed numbers rather than constraining the value's shape, so it isn't expressible here and
 * `100-50` is accepted by this grammar.
 */
class ConfigParseRoutingPolicyRulePortRangeOptionValue : SimpleGrammarOptionValues(
    RULE, range(1, 65536)
)

/**
 * `SuppressPrefixLength=` — table entry config_parse_routing_policy_rule_suppress with ltype 128,
 * which is the inclusive upper bound safe_atoi32's result is checked against.
 */
class ConfigParseRoutingPolicyRuleSuppressPrefixLengthOptionValue : SimpleGrammarOptionValues(
    RULE, SequenceCombinator(unsignedNumber(129), EOF())
)

/** `SuppressInterfaceGroup=` — the same parser with ltype INT32_MAX. */
class ConfigParseRoutingPolicyRuleSuppressInterfaceGroupOptionValue : SimpleGrammarOptionValues(
    RULE, SequenceCombinator(unsignedNumber(2_147_483_648L), EOF())
)
