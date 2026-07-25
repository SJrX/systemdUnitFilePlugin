package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ALTERNATIVE_INTERFACE_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Combinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.HYPHEN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.INTERFACE_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV4_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IntegerTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.TIME_VALUE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * Assorted networkd / udev validators (#509).
 */

/**
 * Validator for `[Match] Name=` in a .network file, and `[Match] Property=`-adjacent name lists.
 *
 * C function: config_parse_match_ifnames (src/shared/net-condition.c) with ltype
 * IFNAME_VALID_ALTERNATIVE — an optional leading `!` that negates the whole list, then a
 * whitespace-separated list of names, each checked by ifname_valid_full().
 *
 * Glob metacharacters need no special handling: `*` and `?` are ordinary valid interface-name
 * characters, so `ve-*` passes the same check a literal name does.
 */
class ConfigParseMatchAlternativeIfnamesOptionValue : SimpleGrammarOptionValues(
    "config_parse_match_ifnames", ifnameList(ALTERNATIVE_INTERFACE_NAME)
)

/**
 * Validator for `[Match] OriginalName=` in a .link file.
 *
 * The same parser with ltype 0, which drops the IFNAME_VALID_ALTERNATIVE flag and so caps each name
 * at IFNAMSIZ - 1 = 15 characters instead of 127.
 */
class ConfigParseMatchIfnamesOptionValue : SimpleGrammarOptionValues(
    "config_parse_match_ifnames", ifnameList(INTERFACE_NAME)
)

private fun ifnameList(name: Combinator): Combinator = SequenceCombinator(
    ZeroOrOne(LiteralChoiceTerminal("!")),
    name,
    ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), name)),
    EOF()
)

/**
 * Validator for `[BridgeVLAN] VLAN=`, `EgressUntagged=` and `PVID=`.
 *
 * C function: config_parse_bridge_vlan_id_range (src/network/networkd-bridge-vlan.c) →
 * parse_vid_range (src/shared/vlan-util.c): a single id or a `low-high` range, each at most
 * VLANID_MAX = 4094.
 *
 * systemd also rejects a range whose low end exceeds its high end; that compares the parsed numbers
 * rather than constraining the shape, so `1000-10` is accepted here.
 */
class ConfigParseBridgeVlanIdRangeOptionValue : SimpleGrammarOptionValues(
    "config_parse_bridge_vlan_id_range",
    SequenceCombinator(
        IntegerTerminal(0, 4095),
        ZeroOrOne(SequenceCombinator(HYPHEN, IntegerTerminal(0, 4095))),
        EOF()
    )
)

/**
 * Validator for `[Tunnel] Key=`, `InputKey=` and `OutputKey=` (.netdev).
 *
 * C function: config_parse_tunnel_key (src/network/netdev/tunnel.c). The value is first tried as an
 * IPv4 address, whose 32 bits are then used as the key; failing that, as a plain uint32. IPv4 comes
 * first here for the same reason it does in systemd — `1.2.3.4` is a key spelled in dotted form, not
 * a malformed number.
 */
class ConfigParseTunnelKeyOptionValue : SimpleGrammarOptionValues(
    "config_parse_tunnel_key",
    SequenceCombinator(AlternativeCombinator(IPV4_ADDR, IntegerTerminal(0L, 4_294_967_296L)), EOF())
)

/**
 * Validator for `[IPv6Prefix] PreferredLifetimeSec=` and `ValidLifetimeSec=`.
 *
 * C function: config_parse_prefix_lifetime (src/network/networkd-radv.c) → parse_sec, with the result
 * additionally required to be under UINT32_MAX seconds. The magnitude check operates on the parsed
 * duration rather than its spelling, so it isn't expressible here.
 */
class ConfigParsePrefixLifetimeOptionValue : SimpleGrammarOptionValues(
    "config_parse_prefix_lifetime", SequenceCombinator(TIME_VALUE, EOF())
)
