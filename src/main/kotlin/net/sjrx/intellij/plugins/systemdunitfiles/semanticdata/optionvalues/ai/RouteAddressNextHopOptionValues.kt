package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN_FALSE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV4_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IP_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IP_ADDR_AND_ANY_PREFIX
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.unsignedNumber
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * The [Route], [Address] and [NextHop] sections of a .network file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BRoute%5D%20Section%20Options
 * parsers https://github.com/systemd/systemd/blob/a8e93919c3/src/network/networkd-route.c    config_parse_route_section
 *         https://github.com/systemd/systemd/blob/a8e93919c3/src/network/networkd-address.c  config_parse_address_section
 *         https://github.com/systemd/systemd/blob/a8e93919c3/src/network/networkd-nexthop.c  config_parse_nexthop_section
 * keys    systemd-build/build/networkd-network-gperf.gperf
 *
 * Each of these is a dispatcher over a ConfigSectionParser table keyed by ltype; the classes below
 * mirror the table entries one by one.
 */

/*
 * Validators for the `[Route]`, `[Address]` and `[NextHop]` sections of a .network file (#509).
 *
 * Like config_parse_routing_policy_rule, each of config_parse_route_section,
 * config_parse_address_section and config_parse_nexthop_section dispatches on its ltype through a
 * ConfigSectionParser table; the classes below mirror the table entries one by one.
 */

private const val ROUTE = "config_parse_route_section"
private const val ADDRESS = "config_parse_address_section"
private const val NEXTHOP = "config_parse_nexthop_section"

/**
 * `[Route] Destination=` / `Source=` — table entry config_parse_route_destination, i.e.
 * in_addr_prefix_from_string_auto: an address of either family with an optional prefix length.
 */
class ConfigParseRouteDestinationOptionValue : SimpleGrammarOptionValues(
    ROUTE, SequenceCombinator(IP_ADDR_AND_ANY_PREFIX, EOF())
)

/**
 * `[Route] PreferredSource=` — table entry config_parse_preferred_src.
 *
 * It first tries parse_boolean() and accepts the value only when it parses as *false*, which is how
 * you forbid a preferred source from a DHCP lease. A true-ish spelling falls through to
 * in_addr_from_string_auto() and is rejected there, so only the negative booleans are listed.
 */
class ConfigParseRoutePreferredSourceOptionValue : SimpleGrammarOptionValues(
    ROUTE,
    SequenceCombinator(
        AlternativeCombinator(
            IP_ADDR,
            BOOLEAN_FALSE,
        ),
        EOF()
    )
)

/** `[Route] GatewayOnLink=` — table entry config_parse_tristate, i.e. parse_boolean() or empty. */
class ConfigParseRouteGatewayOnlinkOptionValue : SimpleGrammarOptionValues(
    ROUTE, SequenceCombinator(BOOLEAN, EOF())
)

/**
 * `[Route] Scope=` — table entry config_parse_route_scope, resolved through route_scope_table. That
 * table is declared with DEFINE_STRING_TABLE_LOOKUP_WITH_FALLBACK(..., UINT8_MAX), so a plain number
 * up to 255 is accepted for scopes the table has no name for.
 */
class ConfigParseRouteScopeOptionValue : SimpleGrammarOptionValues(
    ROUTE,
    SequenceCombinator(
        AlternativeCombinator(
            unsignedNumber(256),
            FlexibleLiteralChoiceTerminal("global", "site", "link", "host", "nowhere"),
        ),
        EOF()
    )
)

/**
 * `[Address] Peer=` — table entry config_parse_address, i.e. config_parse_in_addr_prefix. It is asked
 * for PREFIXLEN_REFUSE, but when that reports a missing prefix length it retries with
 * in_addr_prefix_from_string_auto and only logs a hint, so a bare address is accepted too; the prefix,
 * when present, may be anything the family allows.
 *
 * `Address=` is the same table entry but keeps the older, stricter [NetworkAddressOptionValue] because
 * that behaviour is pinned by existing tests — see the note there.
 */
class ConfigParseAddressSectionPeerOptionValue : SimpleGrammarOptionValues(
    ADDRESS, SequenceCombinator(IP_ADDR_AND_ANY_PREFIX, EOF())
)

/**
 * `[Address] Broadcast=` — table entry config_parse_broadcast: a boolean (asking systemd to derive
 * the broadcast address from Address=, or not to set one), or an explicit IPv4 address. IPv6 is not
 * accepted — the delegate is config_parse_in_addr_non_null with ltype AF_INET.
 */
class ConfigParseAddressSectionBroadcastOptionValue : SimpleGrammarOptionValues(
    ADDRESS, SequenceCombinator(AlternativeCombinator(IPV4_ADDR, BOOLEAN), EOF())
)

/**
 * `[Address] PreferredLifetime=` — table entry config_parse_address_lifetime, whose own comment reads
 * "We accept only 'forever', 'infinity', empty, or '0'". It is not a general time span.
 */
class ConfigParseAddressSectionPreferredLifetimeOptionValue : SimpleGrammarOptionValues(
    ADDRESS, SequenceCombinator(FlexibleLiteralChoiceTerminal("forever", "infinity", "0"), EOF())
)

/**
 * `[NextHop] Gateway=` — table entry config_parse_in_addr_data, i.e. in_addr_from_string_auto: a bare
 * address of either family, with no prefix length.
 */
class ConfigParseNextHopGatewayOptionValue : SimpleGrammarOptionValues(
    NEXTHOP, SequenceCombinator(IP_ADDR, EOF())
)
