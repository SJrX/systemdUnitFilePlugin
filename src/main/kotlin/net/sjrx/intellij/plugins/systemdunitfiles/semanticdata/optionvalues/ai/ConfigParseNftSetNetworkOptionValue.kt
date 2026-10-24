package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Checked
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.OneOrMore
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
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188        extract_first_word (leading separators skipped L39-L56, coalescing L145-L159)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L240-L281       extract_many_words_internal
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

private val NFT_SOURCES = arrayOf("address", "prefix", "ifindex")
private val NFT_FAMILIES = arrayOf("arp", "bridge", "inet", "ip", "ip6", "netdev")
private val NFT_IDENTIFIER_REGEX = Regex("""[A-Za-z](?:[A-Za-z0-9/_.]|\\[\s\S]){0,254}""")

// extract_many_words(":") is called without EXTRACT_DONT_COALESCE_SEPARATORS, so a run of colons is one
// separator, and leading / trailing colons are skipped.
private val COLONS = OneOrMore(COLON)

/**
 * One unquoted `source:family:table:set` tuple. `source` is restricted by NFT_SET_PARSE_NETWORK to
 * `address`, `prefix` or `ifindex` (the cgroup/user/group sources are for unit files).
 */
private val NFT_TUPLE = SequenceCombinator(
    ZeroOrMore(COLON),
    FlexibleLiteralChoiceTerminal(*NFT_SOURCES),
    COLONS,
    FlexibleLiteralChoiceTerminal(*NFT_FAMILIES),
    COLONS,
    NFT_IDENTIFIER,
    COLONS,
    NFT_IDENTIFIER,
    ZeroOrMore(COLON),
)

/**
 * The tuple text after the first-level EXTRACT_UNQUOTE|EXTRACT_RETAIN_ESCAPE split: quotes removed
 * wherever they are, backslashes kept.
 */
private fun stripQuotesRetainEscape(text: String): String {
  val sb = StringBuilder()
  var quote: Char? = null
  for (c in text) {
    when {
      quote != null && c == quote -> quote = null
      quote == null && (c == '"' || c == '\'') -> quote = c
      else -> sb.append(c)
    }
  }
  return sb.toString()
}

/** Whether the (already unquoted) tuple is valid: four non-empty `:`-separated fields, each acceptable. */
private fun nftTupleValid(tuple: String): Boolean {
  val fields = tuple.split(':').filter { it.isNotEmpty() }
  return fields.size == 4 &&
      fields[0] in NFT_SOURCES &&
      fields[1] in NFT_FAMILIES &&
      NFT_IDENTIFIER_REGEX.matches(fields[2]) &&
      NFT_IDENTIFIER_REGEX.matches(fields[3])
}

// A word that contains a quote somewhere (`"address:inet:t:s"`, `address:"inet":t:s`, ...). The first
// split removes the quotes wherever they are, so the word is judged as a whole after unquoting; it is a
// single token because the quotes may cut across the tuple's fields. An unterminated quote does not
// match here and is then flagged by the unquoted branch.
private val QUOTED_NFT_WORD = Checked(
    RegexTerminal(
        """(?=[^\s"']*["'])(?:[^\s"']+|"[^"]*"|'[^']*')+""",
        """(?=[^\s"']*["'])(?:[^\s"']+|"[^"]*"|'[^']*')+"""
    )
) { nftTupleValid(stripQuotesRetainEscape(it)) }

// The quoted form first: it requires a quote, so it never shadows a plain tuple.
private val NFT_WORD = AlternativeCombinator(
    QUOTED_NFT_WORD,
    NFT_TUPLE,
)

/**
 * Validator for the .network `NFTSet=` settings: `[Address]`, `[DHCPv4]`, `[DHCPv6]`,
 * `[IPv6AcceptRA]` and `[DHCPPrefixDelegation]` — config_parse_nft_set with NFT_SET_PARSE_NETWORK,
 * reached either directly or through config_parse_address_section's ADDRESS_NFT_SET table slot.
 *
 * The value is a whitespace-separated list (split with EXTRACT_UNQUOTE|EXTRACT_RETAIN_ESCAPE, so quotes
 * anywhere in a word are removed) of `source:family:table:set` tuples, each split on `:` into exactly
 * four fields (extract_many_words with EXTRACT_CUNESCAPE and without DONT_COALESCE_SEPARATORS: runs of
 * `:` count as one separator and leading/trailing `:` are skipped; a fifth field is an error):
 *  - source: `address`, `prefix` or `ifindex`;
 *  - family: nfproto_table — `arp`, `bridge`, `inet`, `ip`, `ip6`, `netdev`;
 *  - table and set: nft_identifier_valid — a letter followed by letters, digits, `/`, `\`, `_`, `.`,
 *    at most 255 characters.
 * Any invalid tuple aborts the rest of the assignment with a warning.
 *
 * Lenient / strict corners: C escapes inside a table or set name are accepted without being decoded.
 * A C escape that spells part of the source or family (`addr\x65ss`) is flagged, although systemd
 * would decode it; nobody writes that.
 */
class ConfigParseNftSetNetworkOptionValue : SimpleGrammarOptionValues(
    "config_parse_nft_set",
    SequenceCombinator(
        NFT_WORD,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), NFT_WORD)),
        EOF()
    )
)
