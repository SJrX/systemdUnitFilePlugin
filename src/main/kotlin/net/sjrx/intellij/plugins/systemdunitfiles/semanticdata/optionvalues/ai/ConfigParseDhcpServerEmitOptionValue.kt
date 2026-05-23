package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for the [DHCPServer] address-list options: DNS=, NTP=, SIP=, POP3=, SMTP=, LPR=.
 *
 * (Not to be confused with the EmitDNS=, EmitNTP=, ... toggles, which are plain booleans.)
 *
 * C function: config_parse_dhcp_server_emit in src/network/networkd-dhcp-server.c. Each
 * whitespace-separated word is either the literal token `_server_address` (resolved to the
 * server's own address at runtime) or a non-null IPv4 address parsed via in_addr_from_string
 * (AF_INET, ...). The grammar reflects that.
 */
class ConfigParseDhcpServerEmitOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_server_emit",
    SequenceCombinator(
        AlternativeCombinator(LiteralChoiceTerminal("_server_address"), IPV4_ADDR),
        ZeroOrMore(SequenceCombinator(
            WhitespaceTerminal(),
            AlternativeCombinator(LiteralChoiceTerminal("_server_address"), IPV4_ADDR)
        )),
        EOF()
    )
)
