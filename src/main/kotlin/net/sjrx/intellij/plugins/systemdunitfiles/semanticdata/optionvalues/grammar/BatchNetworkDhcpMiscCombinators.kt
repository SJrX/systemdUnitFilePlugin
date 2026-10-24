package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

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
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L403-L427   mangle_base
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L497-L520   safe_atoi
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L522-L558   safe_atollu_full
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L112-L124   parse_ifindex
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/basic/escape.c#L104-L296       cunescape_one
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188  extract_first_word
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L785-L830  ifname_valid_full
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/shared/dns-domain.c#L24-L150   dns_label_unescape
 * leaf https://github.com/systemd/systemd/blob/0f0682b773/src/shared/dns-domain.c#L304-L387  dns_name_concat
 */

/**
 * A boolean as parse_boolean() actually reads it: the twelve spellings of [BOOLEAN_SPELLINGS], compared
 * with STRCASE_IN_SET, i.e. case-insensitively (`Yes`, `TRUE` and `Off` are all accepted). The shared
 * [BOOLEAN] is case-sensitive and therefore flags those; this one doesn't.
 */
val BOOLEAN_ANY_CASE = FlexibleLiteralChoiceTerminal(*BOOLEAN_SPELLINGS, ignoreCase = true)

/** Only the false half of parse_boolean(), case-insensitively — see [BOOLEAN_FALSE] and [BOOLEAN_ANY_CASE]. */
val BOOLEAN_FALSE_ANY_CASE = FlexibleLiteralChoiceTerminal("0", "no", "n", "false", "f", "off", ignoreCase = true)

/**
 * An integer spelled the way systemd's `safe_ato*` helpers read one, range-checked in the base it is
 * actually written in.
 *
 * With base 0 (safe_atou, safe_atou8, safe_atou16, safe_atou32, safe_atoi, safe_atoi32) the helper first
 * runs mangle_base(), which understands Python-style `0b`/`0o` prefixes, and then hands the string to
 * strtoul()/strtol() with base 0, which understands `0x` hex and leading-zero octal. strtoul() also takes
 * an optional sign: `+5` is 5, and for the unsigned helpers `-0` is 0 while any other negative number is
 * -ERANGE. Note mangle_base() runs *before* the sign is looked at, so `+0b1` is not binary (strtoul stops
 * at the `b` and the helper fails) whereas `+0x1` is fine.
 *
 * With [hexOnly] (safe_atoux64 and friends, which pass an explicit base 16) every number is hexadecimal,
 * with an optional `0x` that strtoull itself tolerates.
 *
 * Leading whitespace — which strtoul skips — is NOT consumed here; model it in the enclosing grammar
 * where it can actually occur (unit-file values arrive stripped).
 *
 * [UnsignedNumberTerminal] covers the common subset (decimal/hex/octal, no sign, no `0b`/`0o`); this one
 * exists because the batch needed the signed form and the extra prefixes.
 */
class SafeAtoNumberTerminal(
  private val minInclusive: Long,
  private val maxExclusive: Long,
  private val signed: Boolean = false,
  private val hexOnly: Boolean = false,
) : TerminalCombinator {

  private val shape = if (hexOnly) HEX_SHAPE else BASE0_SHAPE

  private fun span(value: String, offset: Int): String? = shape.matchAt(value, offset)?.value

  /** The value [text] denotes, or null when strtoul would reject it or it overflows a Long. */
  private fun valueOf(text: String): Long? {
    var s = text
    var negative = false
    if (s.startsWith("+") || s.startsWith("-")) {
      negative = s[0] == '-'
      s = s.substring(1)
    }
    val magnitude: Long = magnitudeOf(s, signPresent = text != s) ?: return null
    if (!negative) return magnitude
    if (signed) return -magnitude
    // strtoul happily negates; safe_atou_full then refuses anything but -0.
    return if (magnitude == 0L) 0L else null
  }

  private fun magnitudeOf(s: String, signPresent: Boolean): Long? {
    if (hexOnly) return (if (s.startsWith("0x") || s.startsWith("0X")) s.substring(2) else s).toLongOrNull(16)
    return when {
      s.length > 2 && (s.startsWith("0x") || s.startsWith("0X")) -> s.substring(2).toLongOrNull(16)
      // mangle_base only sees an unsigned string, so `+0b1` / `-0o7` fail in systemd too.
      s.length > 2 && (s.startsWith("0b") || s.startsWith("0B")) -> if (signPresent) null else s.substring(2).toLongOrNull(2)
      s.length > 2 && (s.startsWith("0o") || s.startsWith("0O")) -> if (signPresent) null else s.substring(2).toLongOrNull(8)
      s.length > 1 && s.startsWith("0") -> s.substring(1).toLongOrNull(8)
      else -> s.toLongOrNull()
    }
  }

  // A maxExclusive of Long.MAX_VALUE means "anything that fits a signed 64-bit integer", so that
  // INT64_MAX itself (the SubnetId= ceiling) stays expressible; larger spellings already fail valueOf().
  private fun inRange(text: String): Boolean = valueOf(text)?.let {
    it >= minInclusive && (maxExclusive == Long.MAX_VALUE || it < maxExclusive)
  } ?: false

  override fun SyntacticMatch(value: String, offset: Int): MatchResult {
    val text = span(value, offset) ?: return NoMatch
    return MatchResult(listOf(text), offset + text.length, listOf(this), offset + text.length)
  }

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    val text = span(value, offset) ?: return NoMatch
    if (!inRange(text)) return NoMatch.copy(longestMatch = offset)
    return MatchResult(listOf(text), offset + text.length, listOf(this), offset + text.length)
  }

  override fun parse(value: String, offset: Int): Sequence<ParseStep> {
    val text = span(value, offset) ?: return sequenceOf(Stuck(offset, setOf(this)))
    return sequenceOf(Parse(offset + text.length, listOf(ParsedToken(offset, offset + text.length, text, this, inRange(text)))))
  }

  override fun toString(): String = "SafeAto($minInclusive,$maxExclusive${if (signed) ",signed" else ""}${if (hexOnly) ",hex" else ""})"

  private companion object {
    // Prefixed forms before plain digits so "0x10" isn't read as the single digit 0.
    val BASE0_SHAPE = Regex("""[+-]?(?:0[xX][0-9a-fA-F]+|0[bBoO][0-9]+|[0-9]+)""")
    val HEX_SHAPE = Regex("""[+-]?(?:0[xX])?[0-9a-fA-F]+""")
  }
}

/** One or more `:` — extract_first_word(…, ":", 0) coalesces runs of its separator. */
val COALESCED_COLONS = OneOrMore(COLON)

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

/**
 * A positive interface index, or an alternative interface name (ifname_valid_full with
 * IFNAME_VALID_ALTERNATIVE: 1…127 printable ASCII characters other than `:`, `/`, `%`; not `.`, `..`,
 * `all`, `default`, or all digits). An all-digit spelling is therefore valid exactly when it is a
 * positive index. The digit branch carries a `(?!\S)` guard so that a name like `1eth` is still offered
 * to the name branch rather than stopping at its leading digit.
 */
val IFNAME_OR_IFINDEX = RegexTerminal(
  """\S+""",
  """(?:[0-9]*[1-9][0-9]*(?!\S))|(?!(?:[0-9]+|\.\.?|all|default)(?!$IFNAME_CHAR))$IFNAME_CHAR{1,127}(?!\S)""",
)

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
 * becomes `x`), and the result then goes through dns_label_unescape, which treats a further `\` as the
 * start of a `\\`, `\.` or `\DDD` escape. Modelling both layers exactly isn't worth it, so any
 * backslash-plus-character is accepted as label content (lenient). A label of 64 or more plain characters
 * is refused: that is over DNS_LABEL_MAX whatever follows.
 */
val DNS_WORD_LABEL = RegexTerminal(
  """(?:[^\s.\\]|\\[\s\S])+""",
  """(?!$DNS_PLAIN{64})(?:$DNS_PLAIN|\\[\s\S])+(?=[\s.]|\z)""",
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
 * `TYPE:DATA`, the shape config_parse_dhcp_option() parses and config_parse_dhcp_option_tlv() /
 * config_parse_dhcp6_send_option() parse after their numeric prefix(es). TYPE is one of
 * dhcp_option_data_type_table and decides how the remainder is read:
 *
 *  - `uint8` / `uint16` / `uint32` — safe_atou8/16/32 (base 0, leading whitespace skipped);
 *  - `ipv4address` / `ipv6address` — inet_pton() of that family, no prefix, no scope;
 *  - `string` — cunescape(…, UNESCAPE_ACCEPT_NUL): anything, with valid C escapes; may contain `:`.
 *
 * The separator is extract_first_word(…, ":", 0), which coalesces a run of colons, so `uint8::5` is
 * fine. For `ipv6address` systemd would also swallow the leading colons of the address itself (so
 * `ipv6address:::1` actually fails there); the grammar takes a single colon and accepts it — lenient.
 * The string length check (`check_length`, <= 255 bytes once unescaped) is not modelled.
 */
fun dhcpOptionTypedData(): Combinator = AlternativeCombinator(
  SequenceCombinator(LiteralChoiceTerminal("uint8"), COALESCED_COLONS, ZeroOrOne(WhitespaceTerminal()), SafeAtoNumberTerminal(0, 256L)),
  SequenceCombinator(LiteralChoiceTerminal("uint16"), COALESCED_COLONS, ZeroOrOne(WhitespaceTerminal()), SafeAtoNumberTerminal(0, 65_536L)),
  SequenceCombinator(LiteralChoiceTerminal("uint32"), COALESCED_COLONS, ZeroOrOne(WhitespaceTerminal()), SafeAtoNumberTerminal(0, 4_294_967_296L)),
  SequenceCombinator(LiteralChoiceTerminal("ipv4address"), COALESCED_COLONS, IPV4_ADDR),
  SequenceCombinator(LiteralChoiceTerminal("ipv6address"), COLON, IPV6_ADDR),
  SequenceCombinator(LiteralChoiceTerminal("string"), COALESCED_COLONS, C_ESCAPED_TEXT),
)
