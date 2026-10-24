package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CREDENTIAL_REFERENCE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV4_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV6_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.UINT16_LIMIT
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.safeAtou

/*
 * [WireGuardPeer] Endpoint= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#Endpoint=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/wireguard.c#L751-L862   config_parse_wireguard_endpoint (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/socket-netlink.c#L220-L354   in_addr_port_ifindex_name_from_string_auto (no ifindex, no server name)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L718-L734   parse_ip_port
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/dns-domain.c#L24-L150   dns_label_unescape (via dns_name_is_valid)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/creds-util.c#L156-L180   read_credential (credential_name_valid)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-socket-netlink.c#L356   TEST(in_addr_port_ifindex_name_from_string_auto)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-wireguard.netdev#L17   Endpoint=@network.wireguard.peer0.endpoint
 */

// parse_ip_port: a safe_atou16 number (base 0), not zero.
private val ENDPOINT_PORT = safeAtou(UINT16_LIMIT, 1)

// The hostname fallback: everything up to the LAST colon (strrchr), checked with dns_name_is_valid():
// dot-separated labels of at most 63 characters, each character printable (>= ' ', not DEL) or one of
// the escapes `\.`, `\\` or `\DDD`. A single trailing dot is allowed, and dns_name_is_valid() also
// accepts the root name — `.` or nothing at all — so `:51820` passes the parser too.
private const val DNS_LABEL = """(?:[^.\\\x00-\x1F\x7F]|\\[.\\]|\\[0-9]{3}){1,63}"""
private val ENDPOINT_HOST = RegexTerminal(
    """[^\x00-\x1F\x7F]+(?=:[^:]*$)""",
    """(?:$DNS_LABEL(?:\.$DNS_LABEL)*\.?|\.)(?=:[^:]*$)""",
)

/**
 * Validator for `[WireGuardPeer] Endpoint=` (.netdev).
 *
 * C function: config_parse_wireguard_endpoint. In the order systemd tries them:
 *
 *  - `@name` reads the endpoint from a credential;
 *  - in_addr_port_ifindex_name_from_string_auto with no interface and no server name: `[IPv6]:port`,
 *    `IPv4:port`, or — because that helper makes the port optional — a bare IPv4 or IPv6 address (the
 *    man page says a port is required; the parser stores port 0);
 *  - otherwise `host:port`, split at the last colon, where the host only has to pass
 *    dns_name_is_valid(), which is permissive (any printable characters, labels up to 63 bytes).
 *
 * The port is a parse_ip_port() number, 1…65535 (safe_atou16 base 0, so hex works). Because the host
 * fallback is so permissive, something like `[fe80::1%eth0]:51820` is accepted (as a hostname), as it
 * is by systemd at parse time. What is flagged: a missing or invalid port, empty labels, over-long labels
 * and control characters. An invalid value is logged and ignored by systemd.
 */
class ConfigParseWireguardEndpointOptionValue : SimpleGrammarOptionValues(
    "config_parse_wireguard_endpoint",
    AlternativeCombinator(
        SequenceCombinator(CREDENTIAL_REFERENCE, EOF()),
        SequenceCombinator(LiteralChoiceTerminal("["), IPV6_ADDR, LiteralChoiceTerminal("]"), COLON, ENDPOINT_PORT, EOF()),
        SequenceCombinator(IPV4_ADDR, COLON, ENDPOINT_PORT, EOF()),
        SequenceCombinator(IPV6_ADDR, EOF()),
        SequenceCombinator(IPV4_ADDR, EOF()),
        SequenceCombinator(ZeroOrOne(ENDPOINT_HOST), COLON, ENDPOINT_PORT, EOF()),
    )
)
