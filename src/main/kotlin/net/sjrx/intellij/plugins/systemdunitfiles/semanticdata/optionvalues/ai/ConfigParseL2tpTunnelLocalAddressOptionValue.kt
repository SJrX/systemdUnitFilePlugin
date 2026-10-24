package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ALTERNATIVE_INTERFACE_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV4_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV6_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [L2TP] Local= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#Local=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/l2tp-tunnel.c#L477-L569   config_parse_l2tp_tunnel_local_address (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/l2tp-tunnel.c#L38-L42   l2tp_local_address_type_table
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188   extract_first_word ("@", no flags; leading-separator skip at #L39-L56)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L785-L830   ifname_valid_full (IFNAME_VALID_ALTERNATIVE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L531-L551   in_addr_from_string_auto
 */

/**
 * Validator for `[L2TP] Local=` (.netdev).
 *
 * C function: config_parse_l2tp_tunnel_local_address. `ADDRESS[@INTERFACE]`, where ADDRESS is one of
 * the l2tp_local_address_type_table names `auto`, `static`, `dynamic` or an IPv4/IPv6 literal, and the
 * optional interface (everything after the first `@`) must pass ifname_valid_full with
 * IFNAME_VALID_ALTERNATIVE (up to 127 characters).
 *
 * The `@` is cut with extract_first_word(…, "@", 0), which coalesces a run of separators, so `auto@@eth0`
 * and even a trailing `auto@` (no interface) are accepted; it also skips leading separators, so `@auto`
 * is `auto` (a lone `@` is an empty word and refused). Not modelled: systemd refuses the null
 * address and an address family that conflicts with `Remote=`. An invalid value is logged and ignored.
 */
class ConfigParseL2tpTunnelLocalAddressOptionValue : SimpleGrammarOptionValues(
    "config_parse_l2tp_tunnel_local_address",
    SequenceCombinator(
        // extract_first_word() skips leading separators, so `@auto` reads as `auto`.
        ZeroOrOne(RegexTerminal("@+", "@+")),
        AlternativeCombinator(
            IPV6_ADDR,
            IPV4_ADDR,
            FlexibleLiteralChoiceTerminal("auto", "static", "dynamic"),
        ),
        ZeroOrOne(SequenceCombinator(RegexTerminal("@+", "@+"), ZeroOrOne(ALTERNATIVE_INTERFACE_NAME))),
        EOF()
    )
)
