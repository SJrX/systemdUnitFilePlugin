package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * NFTSet= in the cgroup context ([Service]/[Socket]/[Mount]/[Swap]/[Slice]/[Scope]).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.resource-control.html#NFTSet=family:table:set   (term anchor as generated from the man page)
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L6779-L6795   config_parse_cgroup_nft_set (NFT_SET_PARSE_CGROUP)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/firewall-util.c#L1207-L1295   config_parse_nft_set
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/firewall-util.c#L1136   nft_set_source_table (only cgroup/user/group allowed for NFT_SET_PARSE_CGROUP, #L1258-L1260)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/firewall-util.c#L1125   nfproto_table
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L824   nft_identifier_valid (NFT_NAME_MAXLEN 256)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L240   extract_many_words (":" , EXTRACT_CUNESCAPE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L39-L55   extract_first_word (skips leading separators; trailing ones coalesce at #L151-L158)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-07-PID1.nft.sh#L24   NFTSet=cgroup:inet:sd_test:c user:inet:sd_test:u group:inet:sd_test:g
 * linux  https://www.netfilter.org/projects/nftables/manpage.html   (nft(8): address families, table/set names)
 */

/** The NFT_SET_PARSE_CGROUP sources; address/prefix/ifindex belong to networkd's NFTSet= only. */
private val NFT_CGROUP_SOURCE = FlexibleLiteralChoiceTerminal("cgroup", "user", "group")

/** nfproto_table: the nft address families. */
private val NFT_FAMILY = FlexibleLiteralChoiceTerminal("arp", "bridge", "inet", "ip", "ip6", "netdev")

/**
 * nft_identifier_valid: an ASCII letter, then letters, digits, `/`, `\`, `_` or `.`, shorter than
 * NFT_NAME_MAXLEN (256). The lenient side takes anything up to the next `:` or whitespace so a bad
 * name is located as one token.
 */
private val NFT_IDENTIFIER = RegexTerminal("""[^\s:"']+""", """[A-Za-z][A-Za-z0-9/\\_.]{0,254}""")

/**
 * extract_many_words() is extract_first_word() in a loop, so runs of `:` coalesce (`cgroup::inet:t:s`
 * is still four fields).
 */
private val FIELD_SEPARATOR = RegexTerminal(":+", ":+")

/**
 * extract_first_word() skips leading separators, and after the 4th word it swallows any trailing run of
 * separators up to the end of the string (leaving q == NULL, so the isempty(q) check passes) — hence
 * `:cgroup:inet:t:s` and `cgroup:inet:t:s:` are both accepted.
 */
private val NFT_SET_TUPLE = SequenceCombinator(
    ZeroOrOne(FIELD_SEPARATOR),
    NFT_CGROUP_SOURCE, FIELD_SEPARATOR, NFT_FAMILY, FIELD_SEPARATOR, NFT_IDENTIFIER, FIELD_SEPARATOR, NFT_IDENTIFIER,
    ZeroOrOne(FIELD_SEPARATOR),
)

/** The outer split uses EXTRACT_UNQUOTE, so a whole tuple may be quoted. */
private val NFT_SET_ENTRY = AlternativeCombinator(
    SequenceCombinator(LiteralChoiceTerminal("\""), NFT_SET_TUPLE, LiteralChoiceTerminal("\"")),
    SequenceCombinator(LiteralChoiceTerminal("'"), NFT_SET_TUPLE, LiteralChoiceTerminal("'")),
    NFT_SET_TUPLE,
)

/**
 * Validator for the unit-file `NFTSet=` (cgroup context).
 *
 * A whitespace-separated list of `SOURCE:FAMILY:TABLE:SET` tuples:
 *
 *  - SOURCE is `cgroup`, `user` or `group`. The shared config_parse_nft_set() also knows `address`,
 *    `prefix` and `ifindex`, but rejects them for this ltype ("Unknown NFT source").
 *  - FAMILY is one of `arp`, `bridge`, `inet`, `ip`, `ip6`, `netdev` (exact match).
 *  - TABLE and SET must satisfy nft_identifier_valid().
 *
 * Exactly four fields: extract_many_words() is asked for four and anything left over fails the tuple.
 * Any failure logs a warning and abandons the rest of the line, so one bad tuple is flagged.
 *
 * The man page's term reads `NFTSet=family:table:set`, but both its body text and the C parser require
 * the leading source type; this grammar follows the C.
 *
 * Leading and trailing `:` around a tuple are accepted (see NFT_SET_TUPLE).
 *
 * Known over-rejection: the outer whitespace split uses EXTRACT_UNQUOTE, so quotes *inside* a tuple
 * (`cgroup:inet:"t":s`) are removed by systemd and accepted; this grammar only accepts a tuple quoted
 * as a whole. No real configuration writes that.
 *
 * Deliberately lenient: backslash escapes inside a name (decoded by EXTRACT_CUNESCAPE before the
 * identifier check) are not decoded — `\` is simply accepted as an identifier character, as
 * nft_identifier_valid() does after decoding.
 *
 * Covers: Service.NFTSet, Socket.NFTSet, Mount.NFTSet, Swap.NFTSet, Slice.NFTSet, Scope.NFTSet. (The
 * [Network]/[Address]/… NFTSet= of networkd uses ltype NFT_SET_PARSE_NETWORK and is not covered.)
 *
 * The empty assignment clears the list and never reaches the grammar.
 */
class ConfigParseCgroupNftSetOptionValue : SimpleGrammarOptionValues(
    "config_parse_cgroup_nft_set",
    SequenceCombinator(
        NFT_SET_ENTRY,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), NFT_SET_ENTRY)),
        EOF()
    )
)
