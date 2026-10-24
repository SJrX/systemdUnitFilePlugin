package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore

/*
 * NFTSet= in [Address], [DHCPv4], [DHCPv6], [IPv6AcceptRA] and [DHCPPrefixDelegation] (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#NFTSet=source:family:table:set
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/firewall-util.c#L1207-L1295   config_parse_nft_set (NFT_SET_PARSE_NETWORK)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-address.c#L2298-L2363  config_parse_address_section (ADDRESS_NFT_SET → config_parse_nft_set, NFT_SET_PARSE_NETWORK)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/firewall-util.c#L1125-L1145   nfproto_table, nft_set_source_table
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L824-L835         nft_identifier_valid
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-dhcp-client-ipv4-only.network
 * linux  https://www.netfilter.org/projects/nftables/manpage.html   (NFT_NAME_MAXLEN = 256, include/uapi/linux/netfilter/nf_tables.h)
 */

// nft_identifier_valid: a letter, then letters, digits, `/`, `\`, `_` and `.`; shorter than
// NFT_NAME_MAXLEN (256). The tuple is split with EXTRACT_CUNESCAPE, so a backslash in the file starts a C
// escape; any escaped character is accepted here rather than decoding it.
private val NFT_IDENTIFIER = RegexTerminal(
    """[^\s:"']+""",
    """[A-Za-z](?:[A-Za-z0-9/_.]|\\[\s\S]){0,254}"""
)

/**
 * One `source:family:table:set` tuple. `source` is restricted by NFT_SET_PARSE_NETWORK to `address`,
 * `prefix` or `ifindex` (the cgroup/user/group sources are for unit files).
 */
private val NFT_TUPLE = SequenceCombinator(
    FlexibleLiteralChoiceTerminal("address", "prefix", "ifindex"),
    COLON,
    FlexibleLiteralChoiceTerminal("arp", "bridge", "inet", "ip", "ip6", "netdev"),
    COLON,
    NFT_IDENTIFIER,
    COLON,
    NFT_IDENTIFIER,
)

private val DQ = LiteralChoiceTerminal("\"")
private val SQ = LiteralChoiceTerminal("'")

// The list is split with EXTRACT_UNQUOTE first, so a whole tuple may be quoted.
private val NFT_WORD = AlternativeCombinator(
    SequenceCombinator(DQ, NFT_TUPLE, DQ),
    SequenceCombinator(SQ, NFT_TUPLE, SQ),
    NFT_TUPLE,
)

/**
 * Validator for the .network `NFTSet=` settings: `[Address]`, `[DHCPv4]`, `[DHCPv6]`,
 * `[IPv6AcceptRA]` and `[DHCPPrefixDelegation]` — config_parse_nft_set with NFT_SET_PARSE_NETWORK,
 * reached either directly or through config_parse_address_section's ADDRESS_NFT_SET table slot.
 *
 * The value is a whitespace-separated list (split with EXTRACT_UNQUOTE|EXTRACT_RETAIN_ESCAPE) of
 * `source:family:table:set` tuples, each split on `:` into exactly four fields (extract_many_words with
 * EXTRACT_CUNESCAPE; more or fewer fields is an error):
 *  - source: `address`, `prefix` or `ifindex`;
 *  - family: nfproto_table — `arp`, `bridge`, `inet`, `ip`, `ip6`, `netdev`;
 *  - table and set: nft_identifier_valid — a letter followed by letters, digits, `/`, `\`, `_`, `.`,
 *    at most 255 characters.
 * Any invalid tuple aborts the rest of the assignment with a warning.
 *
 * Lenient / strict corners: a quote that wraps only part of a tuple (`address:"inet":t:s`, which
 * EXTRACT_UNQUOTE would silently remove) is flagged; a fully quoted tuple is accepted. C escapes inside
 * a name are accepted without being decoded.
 */
class ConfigParseNftSetNetworkOptionValue : SimpleGrammarOptionValues(
    "config_parse_nft_set",
    SequenceCombinator(
        NFT_WORD,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), NFT_WORD)),
        EOF()
    )
)
