package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups
import java.math.BigInteger

/*
 * Shared grammar pieces for the network-dhcp-misc validator batch (systemd-networkd DHCPv4/DHCPv6/DHCP
 * server/DUID/DNS/NTP/LLDP and assorted [Network]/[Link] settings).
 *
 * Everything here is pinned to systemd 0f0682b773 (systemd-build/build/last_commit_hash); browse it at
 * https://github.com/systemd/systemd/blob/0f0682b773. These live in their own file rather than in
 * Combinators.kt so that parallel batches don't collide; several of them are candidates for promotion
 * (see the batch's RECOMMENDATIONS.md).
 *
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L23-L46     parse_boolean
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L112-L124   parse_ifindex
 *
 * Integers (safe_atou8/16/32, safe_atoi, safe_atoux64, parse_ifindex) are read with the shared [SafeAtoTerminal].
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/basic/escape.c#L104-L296       cunescape_one
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188  extract_first_word
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L785-L830  ifname_valid_full
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/shared/dns-domain.c#L24-L150   dns_label_unescape
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/shared/dns-domain.c#L304-L387  dns_name_concat
 */

/** One or more `:` — extract_first_word(…, ":", 0) coalesces runs of its separator. */
val NETWORK_DHCP_MISC_COALESCED_COLONS = OneOrMore(COLON)

/**
 * One C escape sequence as cunescape_one() accepts it: `\a \b \f \n \r \t \v \\ \" \' \s`, `\xHH`,
 * `\uHHHH`, `\UHHHHHHHH`, or a three-digit octal `\OOO` no greater than 0377. (cunescape_one also refuses
 * NUL unless asked, UTF-16 surrogates and out-of-range code points; those corners are left lenient.)
 */
const val C_ESCAPE_REGEX = """\\(?:[abfnrtv\\"'s]|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}|U[0-9a-fA-F]{8}|[0-3][0-7]{2})"""

/**
 * A run of text that cunescape() must be able to decode: any characters, with every backslash starting a
 * valid [C_ESCAPE_REGEX]. A stray or trailing backslash is an error (no UNESCAPE_RELAX). Non-empty.
 */
val C_ESCAPED_TEXT = RegexTerminal("""(?:[^\\]|\\[\s\S]?)+""", """(?:[^\\]|$C_ESCAPE_REGEX)+\z""")

/**
 * One word as extract_first_word(…, NULL, EXTRACT_CUNESCAPE|EXTRACT_UNQUOTE) splits it: unquoted text,
 * `"…"` and `'…'` sections (which may contain whitespace) glued together, with backslash escapes —
 * inside or outside quotes — decoded by cunescape_one. An escape that cunescape_one doesn't know (`\q`,
 * `\ `), a trailing backslash or an unterminated quote fails the whole assignment.
 */
val CUNESCAPE_UNQUOTE_WORD = RegexTerminal(
  """(?:[^\s"'\\]|\\[\s\S]?|"(?:[^"\\]|\\[\s\S]?)*"?|'(?:[^'\\]|\\[\s\S]?)*'?)+""",
  """(?:[^\s"'\\]|$C_ESCAPE_REGEX|"(?:[^"\\]|$C_ESCAPE_REGEX)*"|'(?:[^'\\]|$C_ESCAPE_REGEX)*')+(?=\s|\z)""",
)

/**
 * [CUNESCAPE_UNQUOTE_WORD] minus the words that unquote to the empty string (`""`, `''`, `""''`):
 * config_parse_dhcp4_user_class and config_parse_dhcp6_user_or_vendor_class drop those with "The length of
 * the … entry '' is not in the range 1…255/65535, ignoring" (checked with systemd's parser at 0f0682b773).
 * The upper length bound is not modelled (lenient).
 *
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L687-L692  len == 0 check (DHCPv4)
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L732-L737  len == 0 check (DHCPv6)
 */
val NETWORK_DHCP_MISC_NONEMPTY_UNQUOTE_WORD: Combinator = NetworkDhcpMiscCheckedSpan(CUNESCAPE_UNQUOTE_WORD, "NonEmptyWord") {
  !NETWORK_DHCP_MISC_EMPTY_QUOTES.matches(it)
}

private val NETWORK_DHCP_MISC_EMPTY_QUOTES = Regex("""(?:""|'')+""")

/** A whitespace-separated list of [word]s (the value arrives stripped, so no leading/trailing space). */
fun wordList(word: Combinator): Combinator = SequenceCombinator(
  word,
  ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), word)),
)

// A note on the RegexTerminals below: each semantic regex either matches the *whole* span its syntactic
// regex matched or nothing (trailing lookaheads / \z). Under the classic engine a semantic match that
// stops short inside the syntactic span misaligns GrammarOptionValue's token bookkeeping and can index
// past the end of its token list (seen as an IndexOutOfBoundsException on DUIDRawData=000:11:22).

// ---------------------------------------------------------------------------------------------------
// Interface name or index — `parse_ifindex(s) > 0 || ifname_valid_full(s, IFNAME_VALID_ALTERNATIVE)`,
// the pattern used by config_parse_uplink and config_parse_fdb_interface.
private const val IFNAME_CHAR = """[\x21-\x7E&&[^:/%]]"""

private val IFNAME_ALTERNATIVE = Regex("""(?!(?:\.\.?|all|default)\z)$IFNAME_CHAR{1,127}""")

/**
 * A terminal that takes the span [shape] matches and accepts it iff [valid] says so, for leaves whose
 * acceptance is a computation rather than a regular language.
 */
class CheckedTerminal(private val shape: Regex, private val name: String, private val valid: (String) -> Boolean) : TerminalCombinator {

  private fun span(value: String, offset: Int): String? = shape.matchAt(value, offset)?.value?.takeIf { it.isNotEmpty() }

  override fun SyntacticMatch(value: String, offset: Int): MatchResult {
    val text = span(value, offset) ?: return NoMatch
    return MatchResult(listOf(text), offset + text.length, listOf(this), offset + text.length)
  }

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    val text = span(value, offset) ?: return NoMatch
    if (!valid(text)) return NoMatch.copy(longestMatch = offset)
    return MatchResult(listOf(text), offset + text.length, listOf(this), offset + text.length)
  }

  override fun parse(value: String, offset: Int): Sequence<ParseStep> {
    val text = span(value, offset) ?: return sequenceOf(Stuck(offset, setOf(this)))
    return sequenceOf(Parse(offset + text.length, listOf(ParsedToken(offset, offset + text.length, text, this, valid(text)))))
  }

  override fun toString(): String = name
}

/**
 * A positive interface index, or an alternative interface name: `parse_ifindex(s) > 0 ||
 * ifname_valid_full(s, IFNAME_VALID_ALTERNATIVE)`.
 *
 * parse_ifindex() is safe_atoi() (base 0) plus `> 0`, so `010` is index 8, `0x10` is 16 and `+5` is 5, while
 * `0`, `08` and `99999999999` are refused. ifname_valid_full() then takes 1…127 printable ASCII characters
 * other than `:`, `/`, `%`, but not `.`, `..`, `all`, `default`, or a spelling made only of digits (those
 * failed parse_ifindex above, so they are zero, too large or malformed octal).
 *
 * The span is the whole value, whitespace included, because safe_atoi sees all of it: `0b 1`, `0o 7` and `\v5`
 * are indexes 1, 7 and 5, while `eth 0` and `0b 0` are refused (checked with systemd's parser at 0f0682b773).
 */
val IFNAME_OR_IFINDEX = CheckedTerminal(Regex("""[\s\S]+"""), "IfnameOrIfindex") { text ->
  SafeAtoTerminal(
    signed = true,
    base = 0,
    flags = emptySet(),
    minInclusive = 1L,
    maxInclusive = Int.MAX_VALUE.toLong(),
    acceptsWhitespace = true,
    acceptsMinus = true,
  ).valueOf(text) != null ||
    (!text.all { it in '0'..'9' } && IFNAME_ALTERNATIVE.matches(text))
}

// ---------------------------------------------------------------------------------------------------
// DNS names.

// A character that may appear unescaped in a non-LDH DNS label (dns_label_unescape without
// DNS_LABEL_LDH): anything >= 0x20 except DEL, the `.` separator and the `\` escape introducer. In a
// whitespace-separated word list whitespace is also excluded (it ends the word).
private const val DNS_PLAIN = """[^\s.\\\p{Cntrl}]"""

/**
 * One non-LDH DNS label inside a whitespace-separated word.
 *
 * The words are cut by extract_first_word(…, NULL, 0), which strips one level of backslash (a raw `\x`
 * becomes `x`), and the result then goes through dns_label_unescape, which treats a remaining `\` as the
 * start of a `\\`, `\.` or `\DDD` (000…255) escape. So in the raw text:
 *  - `\x` for any `x` other than a backslash is just `x` (accepted as label content);
 *  - a doubled backslash `\\` survives as `\` and must start a valid dns_label_unescape escape: it must
 *    be followed by `\\` or `\.` (an escaped backslash/dot), by three digits no greater than 255, or by
 *    the `.` separator (raw `foo\\.bar` is `foo\.bar`, one label with a literal dot — accepted either
 *    way). `foo\\bar`, `foo\\256` and a trailing `foo\\` are refused, as systemd refuses them.
 *
 * Still lenient: a raw `\.` is treated as label content although extract_first_word turns it into a
 * separator, so `a\..b` and `\.a` (empty labels once unescaped) are accepted. A label of 64 or more
 * plain characters is refused: that is over DNS_LABEL_MAX whatever follows.
 *
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/shared/dns-domain.c#L60-L100  dns_label_unescape escapes
 */
val DNS_WORD_LABEL = RegexTerminal(
  """(?:[^\s.\\]|\\[\s\S])+""",
  """(?!$DNS_PLAIN{64})(?:$DNS_PLAIN|\\[^\\]|\\\\(?:\\[\\.]|[01][0-9]{2}|2[0-4][0-9]|25[0-5]|(?=\.)))+(?=[\s.]|\z)""",
)

/**
 * A DNS name as dns_name_is_valid() accepts it, written as one whitespace-separated word: labels joined
 * by single dots with an optional single trailing dot, or the root domain `.` on its own. Empty labels
 * (`a..b`, `.a`) and a double trailing dot are rejected, as in dns_label_unescape. The 253-character
 * whole-name limit is not enforced.
 */
val DNS_NAME_WORD = AlternativeCombinator(
  SequenceCombinator(DNS_WORD_LABEL, ZeroOrMore(SequenceCombinator(DOT, DNS_WORD_LABEL)), ZeroOrOne(DOT)),
  DOT,
)

/**
 * is_localhost(): `localhost`, `localhost.localdomain` and any name ending in `.localhost` or
 * `.localhost.localdomain`, case-insensitively, with or without a trailing dot. [name] is a raw
 * [DNS_NAME_WORD]; single-backslash escapes are dropped first, as extract_first_word drops them.
 *
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/basic/hostname-util.c#L145-L161  is_localhost
 */
fun networkDhcpMiscIsLocalhost(name: String): Boolean {
  val n = name.replace(Regex("""\\(.)"""), "$1").lowercase().removeSuffix(".")
  return n == "localhost" || n == "localhost.localdomain" || n.endsWith(".localhost") || n.endsWith(".localhost.localdomain")
}

/** An LDH label: letters, digits and inner hyphens, 1…63 characters (dns_label_unescape with DNS_LABEL_LDH). */
val LDH_DNS_LABEL = RegexTerminal(
  """[A-Za-z0-9-]+""",
  """[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?![A-Za-z0-9-])""",
)

/**
 * A DNS name as dns_name_is_valid_ldh() accepts it: [LDH_DNS_LABEL]s joined by single dots, an optional
 * trailing dot, no escapes (DNS_LABEL_NO_ESCAPES), or the root domain `.`. The 253-character limit is not
 * enforced.
 */
val LDH_DNS_NAME = AlternativeCombinator(
  SequenceCombinator(LDH_DNS_LABEL, ZeroOrMore(SequenceCombinator(DOT, LDH_DNS_LABEL)), ZeroOrOne(DOT)),
  DOT,
)

// ---------------------------------------------------------------------------------------------------
// Typed DHCP option payloads — config_parse_dhcp_option / the tail of config_parse_dhcp6_send_option.

/**
 * [IPV6_ADDR] minus the two forms that begin with `::` (`::1`, `::ffff:1.2.3.4`, `::`) — for positions
 * where a preceding coalescing `:` separator has already swallowed any leading colons.
 */
val IPV6_ADDR_NO_LEADING_COLON = Labeled(Role.LITERAL, tag = SemanticTag.IPV6, inner = AlternativeCombinator(
  IPV6_IPV4_SUFFIX_FULL,
  IPV6_IPV4_SUFFIX_ONE_HEXTET_BEFORE_ZERO_COMP,
  IPV6_IPV4_SUFFIX_TWO_HEXTET_BEFORE_ZERO_COMP,
  IPV6_IPV4_SUFFIX_THREE_HEXTET_BEFORE_ZERO_COMP,
  IPV6_IPV4_SUFFIX_FOUR_HEXTET_BEFORE_ZERO_COMP,
  IPV6_IPV4_SUFFIX_FIVE_HEXTET_BEFORE_ZERO_COMP,
  IPV6_FULL_SPECIFIED,
  IPV6_SEVEN_HEXTET_BEFORE_ZERO_COMP,
  IPV6_SIX_HEXTET_BEFORE_ZERO_COMP,
  IPV6_FIVE_HEXTET_BEFORE_ZERO_COMP,
  IPV6_FOUR_HEXTET_BEFORE_ZERO_COMP,
  IPV6_THREE_HEXTET_BEFORE_ZERO_COMP,
  IPV6_TWO_HEXTET_BEFORE_ZERO_COMP,
  IPV6_ONE_HEXTET_BEFORE_ZERO_COMP,
))

/**
 * `TYPE:DATA`, the shape config_parse_dhcp_option() parses and config_parse_dhcp_option_tlv() /
 * config_parse_dhcp6_send_option() parse after their numeric prefix(es). TYPE is one of
 * dhcp_option_data_type_table and decides how the remainder is read:
 *
 *  - `uint8` / `uint16` / `uint32` — safe_atou8/16/32 ([SafeAtoTerminal], base 0);
 *  - `ipv4address` / `ipv6address` — inet_pton() of that family, no prefix, no scope;
 *  - `string` — cunescape(…, UNESCAPE_ACCEPT_NUL): anything, with valid C escapes; may contain `:`.
 *
 * The separator is extract_first_word(…, ":", 0), which coalesces a run of colons, so `uint8::5` is
 * fine. That coalescing also eats the leading colons of the data itself: for `ipv6address` an address
 * beginning with `::` therefore cannot be written at all (`ipv6address:::1` hands inet_pton just `1`
 * and fails), while `ipv6address::2001:db8::1` is fine. Hence [IPV6_ADDR_NO_LEADING_COLON] after
 * the colon run. Likewise a `string` value cannot start with `:`.
 *
 * The number after `uintN:` is a field cut on `:`, so whitespace reaches safe_atou_full, which skips it
 * (acceptsWhitespace = true). The string length check (`check_length`, <= 255 bytes once unescaped) is not
 * modelled.
 */
fun dhcpOptionTypedData(): Combinator = AlternativeCombinator(
  SequenceCombinator(
    LiteralChoiceTerminal("uint8"),
    NETWORK_DHCP_MISC_COALESCED_COLONS,
    SafeAtoTerminal(
      signed = false,
      base = 0,
      flags = emptySet(),
      minInclusive = BigInteger.ZERO,
      maxInclusive = SafeAtoTerminal.UINT8_MAX,
      acceptsWhitespace = true,
      acceptsMinus = true,
    ),
  ),
  SequenceCombinator(
    LiteralChoiceTerminal("uint16"),
    NETWORK_DHCP_MISC_COALESCED_COLONS,
    SafeAtoTerminal(
      signed = false,
      base = 0,
      flags = emptySet(),
      minInclusive = BigInteger.ZERO,
      maxInclusive = SafeAtoTerminal.UINT16_MAX,
      acceptsWhitespace = true,
      acceptsMinus = true,
    ),
  ),
  SequenceCombinator(
    LiteralChoiceTerminal("uint32"),
    NETWORK_DHCP_MISC_COALESCED_COLONS,
    SafeAtoTerminal(
      signed = false,
      base = 0,
      flags = emptySet(),
      minInclusive = BigInteger.ZERO,
      maxInclusive = SafeAtoTerminal.UINT32_MAX,
      acceptsWhitespace = true,
      acceptsMinus = true,
    ),
  ),
  SequenceCombinator(LiteralChoiceTerminal("ipv4address"), NETWORK_DHCP_MISC_COALESCED_COLONS, IPV4_ADDR),
  SequenceCombinator(LiteralChoiceTerminal("ipv6address"), NETWORK_DHCP_MISC_COALESCED_COLONS, IPV6_ADDR_NO_LEADING_COLON),
  SequenceCombinator(LiteralChoiceTerminal("string"), NETWORK_DHCP_MISC_COALESCED_COLONS, C_ESCAPED_TEXT),
)

// ---------------------------------------------------------------------------------------------------
// Time spans — parse_sec() / parse_time(…, USEC_PER_SEC).
//
// leaf https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1146-L1196  extract_multiplier
// leaf https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287  parse_time
// tests https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-time-util.c

// extract_multiplier's table, in table order; the first prefix that matches wins (startswith, no
// longest-match), hence the atomic group. Note: no `ns`/`nsec` — those belong to parse_nsec only.
private const val PARSE_SEC_UNIT_REGEX =
  "(?>seconds|second|sec|s|minutes|minute|min|months|month|M|msec|ms|m|hours|hour|hr|h|days|day|d|weeks|week|w|years|year|y|usec|us|\u03bcs|\u00b5s)"

/**
 * One number of a parse_time() element: strtoll() digits (an optional `+`, never `-`) with an optional
 * `.FRACTION`, or a bare `.FRACTION` (strtoll converts nothing, then the `.` branch runs: `.5s` is
 * legal). A trailing `.` without digits (`5.`, `5.s`) is refused.
 *
 * The lookahead encodes the one adjacency rule of parse_time(): when no unit follows a number, the next
 * element must be separated by whitespace (`5.5.5` and `5+5` fail, `5 .5` and `5s.5` are fine). So a
 * number may only be followed by whitespace, the end, or the first letter of a unit.
 */
private val PARSE_SEC_NUMBER = RegexTerminal(
  """\+?(?:[0-9]+(?:\.[0-9]*)?|\.[0-9]*)""",
  """(?:\+?[0-9]+(?:\.[0-9]+)?|\.[0-9]+)(?=[ \t\n\r]|\z|[smMhdwyu\u03bc\u00b5])""",
)

private val PARSE_SEC_UNIT = RegexTerminal(PARSE_SEC_UNIT_REGEX, PARSE_SEC_UNIT_REGEX)

private val PARSE_SEC_ELEMENT = SequenceCombinator(
  PARSE_SEC_NUMBER,
  ZeroOrOne(SequenceCombinator(ZeroOrOne(WhitespaceTerminal()), PARSE_SEC_UNIT)),
)

/**
 * A time span exactly as parse_sec() reads it: `infinity`, or one or more `NUMBER [UNIT]` elements whose
 * values are summed, with optional whitespace anywhere between a number and its unit and between
 * elements — elements may also be written back to back after a unit (`1h30min`, `55s500ms`, `5s.5`).
 * The default unit is seconds. A `-` anywhere at the start of an element is refused (`-0` too).
 *
 * Overflow (a sum >= USEC_INFINITY) is not modelled. The shared TIME_VALUE in Combinators.kt is both
 * stricter (it needs whitespace between elements and a digit before `.`) and looser (it accepts the
 * `ns`/`nsec` units parse_sec doesn't know); this one follows the C.
 */
val PARSE_SEC_TIMESPAN = AlternativeCombinator(
  LiteralChoiceTerminal("infinity"),
  SequenceCombinator(PARSE_SEC_ELEMENT, ZeroOrMore(SequenceCombinator(ZeroOrOne(WhitespaceTerminal()), PARSE_SEC_ELEMENT))),
)

private val NETWORK_DHCP_MISC_PARSE_SEC_MULTIPLIERS: Map<String, Long> = mapOf(
  "seconds" to 1_000_000L, "second" to 1_000_000L, "sec" to 1_000_000L, "s" to 1_000_000L,
  "minutes" to 60_000_000L, "minute" to 60_000_000L, "min" to 60_000_000L, "m" to 60_000_000L,
  "months" to 2_629_800_000_000L, "month" to 2_629_800_000_000L, "M" to 2_629_800_000_000L,
  "msec" to 1_000L, "ms" to 1_000L,
  "hours" to 3_600_000_000L, "hour" to 3_600_000_000L, "hr" to 3_600_000_000L, "h" to 3_600_000_000L,
  "days" to 86_400_000_000L, "day" to 86_400_000_000L, "d" to 86_400_000_000L,
  "weeks" to 604_800_000_000L, "week" to 604_800_000_000L, "w" to 604_800_000_000L,
  "years" to 31_557_600_000_000L, "year" to 31_557_600_000_000L, "y" to 31_557_600_000_000L,
  "usec" to 1L, "us" to 1L, "\u03bcs" to 1L, "\u00b5s" to 1L,
)

private val NETWORK_DHCP_MISC_PARSE_SEC_STEP =
  Regex("""[ \t\n\r]*\+?([0-9]*(?:\.[0-9]*)?)[ \t\n\r]*($PARSE_SEC_UNIT_REGEX)?""")

/**
 * The duration, in microseconds, that parse_sec() computes for a [PARSE_SEC_TIMESPAN]-shaped [text]
 * (the elements summed, the default unit seconds, `infinity` as [Long.MAX_VALUE]), or null if [text]
 * isn't of that shape. Fractions are evaluated exactly rather than digit-by-digit as the C does; the two
 * differ by less than a microsecond per element.
 */
fun networkDhcpMiscParseSecUsec(text: String): Long? {
  if (text == "infinity") return Long.MAX_VALUE
  var total = java.math.BigDecimal.ZERO
  var i = 0
  while (i < text.length) {
    val m = NETWORK_DHCP_MISC_PARSE_SEC_STEP.matchAt(text, i) ?: return null
    val number = m.groupValues[1]
    if (m.value.isEmpty() || number.isEmpty() || number == ".") return null
    val multiplier = m.groups[2]?.value?.let { NETWORK_DHCP_MISC_PARSE_SEC_MULTIPLIERS[it] ?: return null } ?: 1_000_000L
    total += java.math.BigDecimal(if (number.startsWith(".")) "0$number" else number.trimEnd('.')) * java.math.BigDecimal(multiplier)
    i = m.range.last + 1
  }
  return total.toBigInteger().min(java.math.BigInteger.valueOf(Long.MAX_VALUE)).toLong()
}

/**
 * Wraps [inner] and additionally requires the whole span it matched to satisfy [valid] — for rules
 * that are a computation over a structured value (a minimum duration, an excluded address range, a
 * reserved name) rather than part of its shape. Matching, colouring and completion are [inner]'s; when
 * [valid] fails, the first token of the span is reported as the semantic error (both engines).
 */
class NetworkDhcpMiscCheckedSpan(
  private val inner: Combinator,
  private val name: String,
  private val valid: (String) -> Boolean,
) : Combinator {

  override fun SyntacticMatch(value: String, offset: Int): MatchResult = inner.SyntacticMatch(value, offset)

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    val m = inner.SemanticMatch(value, offset)
    if (m.matchResult == -1 || valid(value.substring(offset, m.matchResult))) return m
    return NoMatch.copy(longestMatch = offset)
  }

  override fun parse(value: String, offset: Int): Sequence<ParseStep> = inner.parse(value, offset).map { step ->
    if (step !is Parse || step.tokens.isEmpty() || valid(value.substring(offset, step.end))) step
    else step.copy(tokens = listOf(step.tokens[0].copy(valid = false)) + step.tokens.drop(1))
  }

  override fun toString(): String = "$name(${inner})"

  override fun toStringIndented(indent: Int): String = "  ".repeat(indent) + "$name(\n" + inner.toStringIndented(indent + 1) + "\n" + "  ".repeat(indent) + ")"
}

// ---------------------------------------------------------------------------------------------------
// Parsers whose accepted syntax depends on the key or section (they test `lvalue` / `section`).

/**
 * A grammar validator for a (parser, ltype) pair whose C code branches on the key name or the section
 * name — e.g. config_parse_dhcp6_send_option (`streq(lvalue, "SendVendorOption")`) and
 * config_parse_uplink (`:none` vs `:self` by section).
 *
 * The registration is still per (parser, ltype), but [generateProblemDescriptors] receives the PSI
 * property, so it asks [select] for the grammar that applies to that (key, section) and validates with
 * it. [combinator] (the [lenientUnion] of all shapes) is what the key-independent features — value
 * colouring, completion, the deprecation annotator — see; it should accept the union of the shapes.
 */
open class KeyOrSectionDispatchingGrammarOptionValue(
  validatorName: String,
  lenientUnion: Combinator,
  private val select: (key: String, section: String?) -> GrammarOptionValue,
) : GrammarOptionValue(validatorName, lenientUnion) {

  override fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
    val section = PsiTreeUtil.getParentOfType(property, UnitFileSectionGroups::class.java)?.sectionName
    select(property.key, section).generateProblemDescriptors(property, holder)
  }
}
