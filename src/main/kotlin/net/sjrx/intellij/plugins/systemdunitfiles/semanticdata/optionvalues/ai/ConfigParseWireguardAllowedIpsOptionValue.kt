package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IP_ADDR_AND_ANY_PREFIX
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [WireGuardPeer] AllowedIPs= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#AllowedIPs=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/wireguard.c#L673-L749   config_parse_wireguard_allowed_ips (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188   extract_first_word ("," WHITESPACE, no flags)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L845-L897   in_addr_prefix_from_string_auto_full
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-wireguard.netdev#L15   AllowedIPs=fd31:bf08:57cb::/48,192.168.26.3/24
 */

// One extract_first_word() separator run; "," and whitespace both separate and runs coalesce.
private val ALLOWED_IPS_SEPARATOR = RegexTerminal("[\\s,]+", "[\\s,]+")

/**
 * Validator for `[WireGuardPeer] AllowedIPs=` (.netdev).
 *
 * C function: config_parse_wireguard_allowed_ips. A list of IPv4/IPv6 addresses with optional prefix
 * lengths (in_addr_prefix_from_string_auto: a missing length means the full width, `/0` is allowed),
 * separated by commas and/or whitespace. Separators coalesce, and leading or trailing ones are harmless.
 * An address that is not properly masked (`192.168.26.3/24`) is accepted and masked with a warning.
 * An invalid entry is logged and skipped by systemd; an empty assignment clears the list.
 */
class ConfigParseWireguardAllowedIpsOptionValue : SimpleGrammarOptionValues(
    "config_parse_wireguard_allowed_ips",
    SequenceCombinator(
        ZeroOrOne(ALLOWED_IPS_SEPARATOR),
        IP_ADDR_AND_ANY_PREFIX,
        ZeroOrMore(SequenceCombinator(ALLOWED_IPS_SEPARATOR, IP_ADDR_AND_ANY_PREFIX)),
        ZeroOrOne(ALLOWED_IPS_SEPARATOR),
        EOF()
    )
)
