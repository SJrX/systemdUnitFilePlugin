package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import java.math.BigInteger

/*
 * Shared helpers for the network-route-addr validator batch (2026-10): [Route], [RoutingPolicyRule],
 * [Address], [Neighbor], [IPv6SendRA] / [IPv6Prefix] / [IPv6RoutePrefix] / [IPv6PREF64Prefix] and the
 * generic strv / string / match parsers.
 *
 * Everything here is pinned to systemd 0f0682b773, the commit recorded in
 * systemd-build/build/last_commit_hash. Browse it at https://github.com/systemd/systemd/blob/0f0682b773.
 *
 * Kept in its own file (rather than Combinators.kt) because several validator batches were written in
 * parallel; the candidates for promotion are listed in the batch RECOMMENDATIONS.md.
 */

// ---------------------------------------------------------------------------------------------------
// Checked — a value-level check on top of a structural grammar.

/**
 * Matches exactly what [inner] matches, but additionally requires [predicate] to hold for the text it
 * consumed.
 *
 * Use it when systemd's acceptance depends on the *value* a well-formed span denotes rather than on its
 * shape — a time span that has to fit in a uint32 of milliseconds, a metric triple that has to be
 * strictly increasing, an address that may not be the ANY address. The structural grammar still drives
 * tokenization and highlighting; only validity changes.
 *
 * When the predicate fails the first token of the span is reported as the bad one (under both engines),
 * since there is no single token to blame for a property of the whole span.
 */
class Checked(private val inner: Combinator, private val predicate: (String) -> Boolean) : Combinator {

  override fun SyntacticMatch(value: String, offset: Int): MatchResult = inner.SyntacticMatch(value, offset)

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    val match = inner.SemanticMatch(value, offset)
    if (match.matchResult == -1) return match
    if (!predicate(value.substring(offset, match.matchResult))) return NoMatch.copy(longestMatch = offset)
    return match
  }

  override fun parse(value: String, offset: Int): Sequence<ParseStep> =
    inner.parse(value, offset).map { step ->
      // The predicate only runs on spans whose tokens are all individually valid, so predicates may assume
      // well-formed input (e.g. a digit run that fits its range) and need not be total.
      if (step is Parse && step.tokens.isNotEmpty() && step.end > offset && step.tokens.all { it.valid } &&
        !predicate(value.substring(offset, step.end))) {
        step.copy(tokens = listOf(step.tokens[0].copy(valid = false)) + step.tokens.drop(1))
      } else {
        step
      }
    }

  override fun toStringIndented(indent: Int): String = "  ".repeat(indent) + "Checked(\n" + inner.toStringIndented(indent + 1) + "\n" + "  ".repeat(indent) + ")"

  override fun toString(): String = toStringIndented(0)
}


// ---------------------------------------------------------------------------------------------------
// safe_atou* (src/basic/parse-util.c, safe_atou_full / mangle_base).

/**
 * The value [text] denotes when read by systemd's `safe_atou*()` helpers with base 0, or null if they
 * would reject it.
 *
 * Those helpers skip leading whitespace, tolerate a single `+` (and `-0`), understand the Python-style
 * `0b`/`0o` prefixes via mangle_base(), and otherwise hand the string to `strtoul(…, 0)`, which reads
 * `0x…` as hex and a leading `0` as octal. The whole string must be consumed.
 */
fun safeAtouValue(text: String): BigInteger? {
  var s = text.trimStart(' ', '\t', '\n', '\r')
  var negative = false
  if (s.startsWith("+")) {
    s = s.substring(1)
  } else if (s.startsWith("-")) {
    negative = true
    s = s.substring(1)
  }
  val (digits, radix) = when {
    s.length > 2 && (s.startsWith("0b") || s.startsWith("0B")) -> s.substring(2) to 2
    s.length > 2 && (s.startsWith("0o") || s.startsWith("0O")) -> s.substring(2) to 8
    s.length > 2 && (s.startsWith("0x") || s.startsWith("0X")) -> s.substring(2) to 16
    s.length > 1 && s.startsWith("0") -> s.substring(1) to 8
    else -> s to 10
  }
  if (digits.isEmpty() || !digits.all { Character.digit(it, radix) >= 0 }) return null
  val v = BigInteger(digits, radix)
  // strtoul() happily negates; safe_atou_full() only lets "-0" through.
  if (negative && v.signum() != 0) return null
  return v
}

/**
 * An unsigned number in `[minInclusive, maxExclusive)` as `safe_atou*()` reads it: decimal, `0x` hex,
 * `0o`/leading-zero octal, `0b` binary, with an optional leading `+`.
 *
 * Wider than [UnsignedNumberTerminal], which predates the `0b`/`0o`/`+` spellings. The syntactic shape
 * is any run starting with a digit (or sign) and continuing with alphanumerics, so a malformed number
 * like `12abc` is located as one token; use it only where the number is followed by a separator
 * (whitespace, `/`, `-`, `:`, `@`) or the end of the value.
 *
 * safe_atou*() skip leading whitespace themselves. Pass [allowLeadingWhitespace] where the number comes
 * straight after a non-whitespace separator that systemd splits on itself (`100: 200` in a `:` list,
 * `::/ 64` after a prefix slash), so that whitespace is part of the number token. Leave it off where the
 * value was already split into words on whitespace, or where whitespace is the separator.
 */
class SafeAtouTerminal(
  private val minInclusive: Long,
  private val maxExclusive: Long,
  private val allowLeadingWhitespace: Boolean = false,
) : TerminalCombinator {

  private fun span(value: String, offset: Int): String? = (if (allowLeadingWhitespace) SHAPE_WS else SHAPE).matchAt(value, offset)?.value

  private fun inRange(text: String): Boolean {
    val v = safeAtouValue(text) ?: return false
    return v >= BigInteger.valueOf(minInclusive) && v < BigInteger.valueOf(maxExclusive)
  }

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

  override fun toString(): String = "SafeAtou($minInclusive,$maxExclusive)"

  private companion object {
    val SHAPE = Regex("""[+-]?[0-9][0-9a-zA-Z]*""")
    val SHAPE_WS = Regex("""[ \t]*[+-]?[0-9][0-9a-zA-Z]*""")
  }
}

const val UINT32_LIMIT = 4_294_967_296L


// ---------------------------------------------------------------------------------------------------
// parse_sec() — parse_time(t, &usec, USEC_PER_SEC) in src/basic/time-util.c.
//
// The grammar mirrors parse_time()'s loop: a run of `NUMBER [whitespace] [UNIT]` elements, where
//  - NUMBER is strtoll() base 10 (so an optional `+`) with an optional `.fraction`; `.5` is fine, `5.`
//    is not ("Don't allow … 3.sec");
//  - elements may abut (`1h30min`, `5s+3s`) or be separated by whitespace;
//  - a number with no unit defaults to seconds and must then be followed by whitespace or the end —
//    extract_multiplier() refuses `12.34.56` but accepts `12.34 .56`;
//  - `infinity` stands alone.
// Units are matched by a FlexibleLiteralChoiceTerminal: a unit is always followed by a digit, a sign,
// whitespace or the end, so the terminal's greedy letter run lines up with extract_multiplier()'s
// longest-prefix-first table and a misspelt unit is highlighted as one token.

/** extract_multiplier()'s table, in microseconds. */
private val TIME_UNIT_USEC: Map<String, BigInteger> = run {
  val sec = BigInteger.valueOf(1_000_000L)
  val min = sec.multiply(BigInteger.valueOf(60))
  val hour = min.multiply(BigInteger.valueOf(60))
  val day = hour.multiply(BigInteger.valueOf(24))
  mapOf(
    "seconds" to sec, "second" to sec, "sec" to sec, "s" to sec,
    "minutes" to min, "minute" to min, "min" to min,
    "months" to sec.multiply(BigInteger.valueOf(2_629_800L)), "month" to sec.multiply(BigInteger.valueOf(2_629_800L)),
    "M" to sec.multiply(BigInteger.valueOf(2_629_800L)),
    "msec" to BigInteger.valueOf(1000), "ms" to BigInteger.valueOf(1000),
    "m" to min,
    "hours" to hour, "hour" to hour, "hr" to hour, "h" to hour,
    "days" to day, "day" to day, "d" to day,
    "weeks" to day.multiply(BigInteger.valueOf(7)), "week" to day.multiply(BigInteger.valueOf(7)), "w" to day.multiply(BigInteger.valueOf(7)),
    "years" to sec.multiply(BigInteger.valueOf(31_557_600L)), "year" to sec.multiply(BigInteger.valueOf(31_557_600L)),
    "y" to sec.multiply(BigInteger.valueOf(31_557_600L)),
    "usec" to BigInteger.ONE, "us" to BigInteger.ONE, "μs" to BigInteger.ONE, "µs" to BigInteger.ONE,
  )
}

/** USEC_INFINITY, i.e. UINT64_MAX. */
val USEC_INFINITY: BigInteger = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE)
val USEC_PER_SEC: BigInteger = BigInteger.valueOf(1_000_000L)
val USEC_PER_MSEC: BigInteger = BigInteger.valueOf(1_000L)

// `+.5` is refused: strtoll() reads no digits after the sign, so e == p and *e is not '.'.
private val TIME_NUMBER = RegexTerminal("""[+-]?(?:[0-9]+(?:\.[0-9]*)?|\.[0-9]*)""", """(?:\+?[0-9]+(?:\.[0-9]+)?|\.[0-9]+)""")
private val TIME_UNIT = FlexibleLiteralChoiceTerminal(*TIME_UNIT_USEC.keys.toTypedArray())
private val TIME_INFINITY = FlexibleLiteralChoiceTerminal("infinity")

private val TIME_ELEMENT = AlternativeCombinator(
  SequenceCombinator(TIME_NUMBER, ZeroOrOne(WhitespaceTerminal()), TIME_UNIT),
  SequenceCombinator(TIME_NUMBER, AlternativeCombinator(WhitespaceTerminal(), EOF())),
)

/** Every spelling parse_sec() accepts, structurally; not range-checked. Not followed by EOF. */
val PARSE_SEC = AlternativeCombinator(
  TIME_INFINITY,
  OneOrMore(SequenceCombinator(TIME_ELEMENT, ZeroOrOne(WhitespaceTerminal()))),
)

private val TIME_ELEMENT_REGEX = Regex("""\s*(?:\+(?=[0-9]))?([0-9]*)(?:\.([0-9]+))?\s*(seconds|second|sec|s|minutes|minute|min|months|month|M|msec|ms|m|hours|hour|hr|h|days|day|d|weeks|week|w|years|year|y|usec|us|μs|µs)?""")

/**
 * The value parse_sec() gives [text] in microseconds — [USEC_INFINITY] for `infinity` — or null if
 * parse_sec() would fail (including its -ERANGE overflow checks). [text] is expected to be something
 * [PARSE_SEC] matched.
 */
fun parseSecUsec(text: String): BigInteger? {
  val t = text.trim()
  if (t == "infinity") return USEC_INFINITY
  var pos = 0
  var usec = BigInteger.ZERO
  var something = false
  while (pos < t.length) {
    val m = TIME_ELEMENT_REGEX.matchAt(t, pos) ?: return null
    if (m.value.isEmpty()) return null
    val whole = m.groupValues[1]
    val frac = m.groupValues[2]
    if (whole.isEmpty() && frac.isEmpty()) return null
    val multiplier = TIME_UNIT_USEC[m.groupValues[3]] ?: USEC_PER_SEC
    val l = if (whole.isEmpty()) BigInteger.ZERO else BigInteger(whole)
    if (l >= USEC_INFINITY.divide(multiplier)) return null
    usec = usec.add(l.multiply(multiplier))
    var scale = multiplier.divide(BigInteger.TEN)
    for (c in frac) {
      usec = usec.add(BigInteger.valueOf((c - '0').toLong()).multiply(scale))
      scale = scale.divide(BigInteger.TEN)
    }
    if (usec >= USEC_INFINITY) return null
    something = true
    pos = m.range.last + 1
  }
  return if (something) usec else null
}

/**
 * A parse_sec() value whose microsecond count must satisfy [accept] (which receives [USEC_INFINITY] for
 * `infinity`). Not followed by EOF.
 */
fun timeSpan(accept: (BigInteger) -> Boolean = { true }): Combinator =
  Checked(PARSE_SEC) { text -> parseSecUsec(text)?.let(accept) ?: false }

/** DIV_ROUND_UP(a, b) for non-negative a. */
fun divRoundUp(a: BigInteger, b: BigInteger): BigInteger = a.add(b).subtract(BigInteger.ONE).divide(b)


// ---------------------------------------------------------------------------------------------------
// parse_size(t, 1024, …) — src/basic/parse-util.c.
//
// A run of `NUMBER [whitespace] [SUFFIX]` elements where each suffix must be strictly smaller than the
// previous one (start_pos), a bare number (the "" suffix, bytes) ends the run, and NUMBER is strtoull()
// base 10 with an optional fraction (`10.M` is accepted: "Ignore failure").

private val SIZE_NUMBER = RegexTerminal("""[+-]?[0-9]+(?:\.[0-9]*)?""", """\+?[0-9]+(?:\.[0-9]*)?""")
private val SIZE_SUFFIX = LiteralChoiceTerminal("E", "P", "T", "G", "M", "K", "B")
private val SIZE_ELEMENT = SequenceCombinator(SIZE_NUMBER, ZeroOrOne(WhitespaceTerminal()), ZeroOrOne(SIZE_SUFFIX))

/** Every spelling parse_size() (base 1024) accepts, structurally. Not followed by EOF. */
val PARSE_SIZE_IEC = SequenceCombinator(SIZE_ELEMENT, ZeroOrMore(SequenceCombinator(ZeroOrOne(WhitespaceTerminal()), SIZE_ELEMENT)))

private val SIZE_ELEMENT_REGEX = Regex("""\s*\+?([0-9]+)(?:\.([0-9]*))?\s*([EPTGMKB]?)""")
private val SIZE_ORDER = listOf("E", "P", "T", "G", "M", "K", "B", "")

/** The byte count parse_size(text, 1024) yields, or null if it would fail. */
fun parseSizeIec(text: String): BigInteger? {
  val t = text.trim()
  var pos = 0
  var total = BigInteger.ZERO
  var startPos = 0
  val max = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE)
  if (t.isEmpty()) return null
  while (pos < t.length) {
    val m = SIZE_ELEMENT_REGEX.matchAt(t, pos) ?: return null
    val suffix = m.groupValues[3]
    val index = SIZE_ORDER.indexOf(suffix)
    if (index < startPos) return null
    val factor = BigInteger.valueOf(1024).pow(6 - minOf(index, 6))
    val whole = BigInteger(m.groupValues[1])
    val fracDigits = m.groupValues[2]
    var bytes = whole.multiply(factor)
    if (fracDigits.isNotEmpty()) {
      bytes = bytes.add(factor.multiply(BigInteger(fracDigits)).divide(BigInteger.TEN.pow(fracDigits.length)))
    }
    total = total.add(bytes)
    if (total > max) return null
    startPos = index + 1
    pos = m.range.last + 1
    if (m.value.isEmpty()) return null
  }
  return total
}


// ---------------------------------------------------------------------------------------------------
// extract_first_word() word lists (src/basic/extract-word.c).

/**
 * One word as extract_first_word(…, EXTRACT_UNQUOTE|EXTRACT_RETAIN_ESCAPE) reads it: runs of bare
 * characters and `"…"` / `'…'` quoted pieces, glued together (`a"b c"d` is the single word `ab cd`).
 * With EXTRACT_RETAIN_ESCAPE a backslash is an ordinary character, even inside quotes, so the only way
 * such a word can fail is an unterminated quote. The quoted pieces match leniently without their
 * closing quote so the unterminated piece is the token highlighted.
 */
val RETAIN_ESCAPE_WORD: Combinator = OneOrMore(
  AlternativeCombinator(
    RegexTerminal("\"[^\"]*\"?", "\"[^\"]*\""),
    RegexTerminal("'[^']*'?", "'[^']*'"),
    RegexTerminal("[^\\s\"']+", "[^\\s\"']+"),
  )
)

/** A whitespace-separated list of [RETAIN_ESCAPE_WORD]s, followed by EOF. */
val RETAIN_ESCAPE_WORD_LIST: Combinator = SequenceCombinator(
  RETAIN_ESCAPE_WORD,
  ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), RETAIN_ESCAPE_WORD)),
  EOF(),
)

// The C escapes cunescape_one() understands (src/basic/escape.c). NUL and surrogate code points, which
// it refuses, are not singled out.
private const val C_ESCAPE = """\\(?:[abfnrtv\\"'s]|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}|U[0-9a-fA-F]{8}|[0-3][0-7]{2})"""

/**
 * One word as extract_first_word(…, EXTRACT_CUNESCAPE|EXTRACT_UNQUOTE) reads it: like
 * [RETAIN_ESCAPE_WORD], but a backslash starts a C escape both inside and outside quotes, and an
 * unknown escape is an error.
 */
val CUNESCAPE_WORD: Combinator = OneOrMore(
  AlternativeCombinator(
    RegexTerminal("\"(?:[^\"\\\\]|\\\\[\\s\\S])*\"?", "\"(?:[^\"\\\\]|$C_ESCAPE)*\""),
    RegexTerminal("'(?:[^'\\\\]|\\\\[\\s\\S])*'?", "'(?:[^'\\\\]|$C_ESCAPE)*'"),
    RegexTerminal("(?:[^\\s\"'\\\\]|\\\\[\\s\\S])+", "(?:[^\\s\"'\\\\]|$C_ESCAPE)+"),
  )
)

/**
 * [text] with its quotes removed and its escapes collapsed to the escaped character — enough to look at
 * the word's structure (where its `=` is, what precedes it). Escapes are not decoded faithfully.
 */
fun unquoteWord(text: String): String {
  val sb = StringBuilder()
  var quote: Char? = null
  var i = 0
  while (i < text.length) {
    val c = text[i]
    when {
      c == '\\' && i + 1 < text.length -> {
        sb.append(text[i + 1]); i += 2; continue
      }
      quote != null && c == quote -> quote = null
      quote == null && (c == '"' || c == '\'') -> quote = c
      else -> sb.append(c)
    }
    i++
  }
  return sb.toString()
}

/**
 * A whitespace-separated list of interface names as extract_first_word(…, 0) splits it — no quote
 * handling, so a name is simply a run of non-whitespace characters — each checked with [name].
 */
fun plainNameList(name: Combinator): Combinator = SequenceCombinator(
  name,
  ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), name)),
  EOF(),
)


// ---------------------------------------------------------------------------------------------------
// Address literals, as bytes — for the value checks inet_pton() parsing alone cannot express.

/** The 4 or 16 bytes an IPv4 / IPv6 literal (as matched by [IP_ADDR]) denotes, or null. */
fun ipLiteralBytes(text: String): ByteArray? = if (':' in text) ipv6Bytes(text) else ipv4Bytes(text)

private fun ipv4Bytes(text: String): ByteArray? {
  val parts = text.split('.')
  if (parts.size != 4) return null
  val out = ByteArray(4)
  for ((i, p) in parts.withIndex()) {
    val v = p.toIntOrNull() ?: return null
    if (v !in 0..255) return null
    out[i] = v.toByte()
  }
  return out
}

private fun ipv6Bytes(text: String): ByteArray? {
  var s = text
  var tail = ByteArray(0)
  // An embedded dotted-quad supplies the last two groups.
  val lastColon = s.lastIndexOf(':')
  if ('.' in s.substring(lastColon + 1)) {
    tail = ipv4Bytes(s.substring(lastColon + 1)) ?: return null
    s = s.substring(0, lastColon + 1) + "0:0"
  }
  val dc = s.indexOf("::")
  val groups: List<String> = if (dc >= 0) {
    val head = s.substring(0, dc).split(':').filter { it.isNotEmpty() }
    val rest = s.substring(dc + 2).split(':').filter { it.isNotEmpty() }
    val missing = 8 - head.size - rest.size
    if (missing < 1) return null
    head + List(missing) { "0" } + rest
  } else {
    s.split(':')
  }
  if (groups.size != 8) return null
  val out = ByteArray(16)
  for ((i, g) in groups.withIndex()) {
    if (g.isEmpty() || g.length > 4) return null
    val v = g.toIntOrNull(16) ?: return null
    out[2 * i] = (v shr 8).toByte()
    out[2 * i + 1] = v.toByte()
  }
  if (tail.isNotEmpty()) tail.copyInto(out, 12)
  return out
}

/** in_addr_is_null(): every byte zero. */
fun ipIsNull(bytes: ByteArray): Boolean = bytes.all { it.toInt() == 0 }

/** in_addr_is_multicast(): 224.0.0.0/4 for IPv4, ff00::/8 for IPv6. */
fun ipIsMulticast(bytes: ByteArray): Boolean =
  if (bytes.size == 4) (bytes[0].toInt() and 0xF0) == 0xE0 else (bytes[0].toInt() and 0xFF) == 0xFF


// ---------------------------------------------------------------------------------------------------
// parse_boolean() (src/basic/parse-util.c) compares with STRCASE_IN_SET, so `Yes`, `TRUE` and `Off` are
// all booleans. The shared [BOOLEAN] terminal is case-sensitive; this one is not.
val BOOLEAN_ANY_CASE = FlexibleLiteralChoiceTerminal(*BOOLEAN_SPELLINGS, ignoreCase = true)


// ---------------------------------------------------------------------------------------------------
// IPv6 prefixes — in_addr_prefix_from_string(…, AF_INET6, …) (src/basic/in-addr-util.c): an IPv6 literal
// and an optional `/LEN`, where LEN goes through in_addr_parse_prefixlen → safe_atou8 (base 0) and must
// be ≤ 128 (safe_atou8 skips whitespace, so `::/ 64` is fine). Without a slash the prefix length is 128.
// Not followed by EOF.
val IPV6_PREFIX_SAFE_LEN: Combinator = SequenceCombinator(IPV6_ADDR, ZeroOrOne(SequenceCombinator(CIDR_SEPARATOR, SafeAtouTerminal(0, 129, allowLeadingWhitespace = true))))
