package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import java.math.BigDecimal
import java.math.BigInteger

/*
 * Shared grammars for the systemd-networkd traffic-control validators (.network [QDisc]-family and
 * [...Class] sections, systemd src/network/tc/), written for the 2026-10 qdisc batch.
 *
 * Everything here is pinned to systemd 0f0682b773 (systemd-build/build/last_commit_hash):
 * https://github.com/systemd/systemd/blob/0f0682b773
 *
 * The tc parsers lean on four leaf helpers. safe_atou / safe_atou32 / safe_atou8 /
 * safe_atou32_full(.., 16) (https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429)
 * are the shared [SafeAtoTerminal], written out at each call site. The other three are modelled here as
 * closely to the C as the combinator framework allows:
 *
 *  - parse_sec / parse_time                                            -> [parseSec]
 *    https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198
 *  - parse_size(.., 1000 | 1024)                                       -> [parseSize]
 *    https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L157
 *  - parse_permyriad (via parse_tc_percent)                            -> [TC_PERCENT]
 *    https://github.com/systemd/systemd/blob/0f0682b773/src/basic/percent-util.c#L149
 *
 * These are more faithful than the older shared terminals in Combinators.kt (TIME_VALUE rejects
 * "1min30s"; ByteSizeTerminal rejects "1G 512M"). They live in this file only so the batch does not
 * collide with parallel edits to Combinators.kt; see RECOMMENDATIONS.md for promoting
 * them.
 */

// ---------------------------------------------------------------------------------------------------
// Generic helpers

private val OPT_WS = ZeroOrOne(WhitespaceTerminal())

/**
 * Wraps [inner] and additionally requires the text it matched to satisfy [accept].
 *
 * Used for value-level range checks that no single terminal can see — e.g. a parse_size() value
 * written as several elements (`1G 512M`), where the bound applies to the sum. Matching (and so
 * syntax highlighting and error localisation) is delegated to [inner] unchanged; only validity is
 * narrowed. When [accept] returns null ("could not evaluate") the value is accepted: the grammar has
 * already decided it is well-formed, and an evaluator gap must never flag a valid value.
 *
 * Empty matches are never accepted, so a grammar built only of optional parts (like [parseSize]'s
 * chain of optional elements) cannot match the empty string.
 */
class ValueChecked(private val inner: Combinator, private val accept: (String) -> Boolean?) : Combinator {

  private fun ok(text: String): Boolean = accept(text) ?: true

  override fun SyntacticMatch(value: String, offset: Int): MatchResult {
    val m = inner.SyntacticMatch(value, offset)
    return if (m.matchResult == offset) NoMatch.copy(longestMatch = offset) else m
  }

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    val m = inner.SemanticMatch(value, offset)
    if (m.matchResult == -1) return m
    if (m.matchResult == offset) return NoMatch.copy(longestMatch = offset)
    return if (ok(value.substring(offset, m.matchResult))) m else NoMatch.copy(longestMatch = offset)
  }

  override fun parse(value: String, offset: Int): Sequence<ParseStep> =
    inner.parse(value, offset).map { step ->
      when (step) {
        is Stuck -> step
        is Parse -> when {
          step.end == offset -> Stuck(offset, setOf(this))
          ok(value.substring(offset, step.end)) -> step
          // Out of range: keep the tokens (so the span still highlights) but mark them invalid.
          else -> Parse(step.end, step.tokens.map { it.copy(valid = false) }, step.regions)
        }
      }
    }

  override fun toStringIndented(indent: Int): String = "  ".repeat(indent) + "Checked(\n" + inner.toStringIndented(indent + 1) + "\n" + "  ".repeat(indent) + ")"

  override fun toString(): String = toStringIndented(0)
}

// ---------------------------------------------------------------------------------------------------
// Bounds

/** UINT32_MAX, for the size/time range checks. */
private val UINT32_MAX_BIG: BigInteger = SafeAtoTerminal.UINT32_MAX

/** ULLONG_MAX: parse_size() and parse_time() return ERANGE above it. */
private val UINT64_MAX_BIG: BigInteger = SafeAtoTerminal.UINT64_MAX

// ---------------------------------------------------------------------------------------------------
// parse_sec / parse_time

// extract_multiplier()'s table, in table order: the C takes the first entry the text starts with,
// and a regex alternation does the same (time-util.c#L1147-L1196).
private const val SEC_SUFFIX_ALTERNATION =
  "seconds|second|sec|s|minutes|minute|min|months|month|M|msec|ms|m|hours|hour|hr|h|days|day|d|" +
    "weeks|week|w|years|year|y|usec|us|μs|µs"

private val SEC_SUFFIX = Labeled(Role.ENUM, RegexTerminal(SEC_SUFFIX_ALTERNATION, SEC_SUFFIX_ALTERNATION))

// strtoll() digits with an optional '+', then an optional '.digits'; or a bare '.digits' (".5s" is
// legal: strtoll consumes nothing, and the '.' branch takes over). "5." and "+.5" are not.
private const val SEC_NUMBER_RE = """\+?[0-9]+(?:\.[0-9]+)?|\.[0-9]+"""
private val SEC_NUMBER = Labeled(Role.LITERAL, RegexTerminal(SEC_NUMBER_RE, SEC_NUMBER_RE))

// After a number comes either a unit (optionally surrounded by whitespace), or whitespace, or the end.
// A unit may be followed directly by the next number ("5s500ms", "3.1s.2"); a bare number may not
// ("12.34.56" is EINVAL).
private val SEC_ELEMENT = SequenceCombinator(
  SEC_NUMBER,
  AlternativeCombinator(
    SequenceCombinator(OPT_WS, SEC_SUFFIX, OPT_WS),
    WhitespaceTerminal(),
    EOF(),
  ),
)

private val PARSE_SEC = AlternativeCombinator(
  LiteralChoiceTerminal("infinity"),
  OneOrMore(SEC_ELEMENT),
)

private val USEC_INFINITY: BigInteger = UINT64_MAX_BIG

private val LLONG_MAX_BIG: BigInteger = BigInteger.valueOf(Long.MAX_VALUE)

/** Returned by [evaluateParseSec] when parse_time() would fail with ERANGE; above every accepted bound. */
private val PARSE_SEC_ERANGE: BigInteger = USEC_INFINITY + BigInteger.ONE

private val SEC_MULTIPLIERS: Map<String, BigInteger> = run {
  val sec = 1_000_000L
  val minute = 60 * sec
  val hour = 60 * minute
  val day = 24 * hour
  mapOf(
    "seconds" to sec, "second" to sec, "sec" to sec, "s" to sec,
    "minutes" to minute, "minute" to minute, "min" to minute, "m" to minute,
    "months" to 2_629_800L * sec, "month" to 2_629_800L * sec, "M" to 2_629_800L * sec,
    "msec" to 1000L, "ms" to 1000L,
    "hours" to hour, "hour" to hour, "hr" to hour, "h" to hour,
    "days" to day, "day" to day, "d" to day,
    "weeks" to 7 * day, "week" to 7 * day, "w" to 7 * day,
    "years" to 31_557_600L * sec, "year" to 31_557_600L * sec, "y" to 31_557_600L * sec,
    "usec" to 1L, "us" to 1L, "μs" to 1L, "µs" to 1L,
  ).mapValues { BigInteger.valueOf(it.value) }
}

private val SEC_EVAL_ELEMENT = Regex("""\s*\+?([0-9]*)(?:\.([0-9]+))?\s*($SEC_SUFFIX_ALTERNATION)?""")

/**
 * parse_time(t, USEC_PER_SEC) as a value in microseconds, or null if [text] is not evaluable. Overflow
 * (C's ERANGE: an integer part above LLONG_MAX, or a finite total reaching USEC_INFINITY) yields a value
 * greater than USEC_INFINITY so that every bound check rejects it.
 */
fun evaluateParseSec(text: String): BigInteger? {
  val t = text.trim()
  if (t == "infinity") return USEC_INFINITY
  var pos = 0
  var total = BigInteger.ZERO
  while (pos < t.length) {
    while (pos < t.length && t[pos].isWhitespace()) pos++
    if (pos >= t.length) break
    val m = SEC_EVAL_ELEMENT.matchAt(t, pos) ?: return null
    if (m.value.isEmpty()) return null
    pos += m.value.length
    val multiplier = m.groupValues[3].takeIf { it.isNotEmpty() }?.let { SEC_MULTIPLIERS[it] } ?: SEC_MULTIPLIERS.getValue("s")
    val whole = BigInteger(m.groupValues[1].ifEmpty { "0" })
    // strtoll() sets ERANGE above LLONG_MAX; then `(usec_t) l >= USEC_INFINITY / multiplier` and
    // `k >= USEC_INFINITY - usec` are ERANGE too, so a finite total can never reach USEC_INFINITY.
    if (whole > LLONG_MAX_BIG || whole >= USEC_INFINITY / multiplier) return PARSE_SEC_ERANGE
    val k = whole * multiplier
    if (k >= USEC_INFINITY - total) return PARSE_SEC_ERANGE
    total += k
    // The C adds the fractional digits one by one, each times a truncated multiplier/10^k.
    var step = multiplier / BigInteger.TEN
    for (digit in m.groupValues[2]) {
      val kf = BigInteger.valueOf((digit - '0').toLong()) * step
      if (kf >= USEC_INFINITY - total) return PARSE_SEC_ERANGE
      total += kf
      step /= BigInteger.TEN
    }
  }
  return total
}

/**
 * A whole parse_sec() value: `infinity`, or one or more `number[unit]` elements.
 *
 * Mirrors parse_time() (https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287)
 * including the forms its unit test asserts are legal (test-time-util.c#L21): `5s500ms`, `.22s`,
 * `+3.1s`, `3.1s.2`, `3.1 .2`, `23µs`; and rejects what it asserts are not: `5.`, `.s`, `-5s`,
 * `3.s`, `3. 1s`, `12.34.56`, `infinity .7`.
 *
 * @param maxUsec when set, the value (in microseconds, `infinity` counting as USEC_INFINITY) must not
 *   exceed it.
 */
fun parseSec(maxUsec: BigInteger? = null): Combinator =
  ValueChecked(PARSE_SEC) { text ->
    evaluateParseSec(text)?.let { it <= (maxUsec ?: USEC_INFINITY) }
  }

/** [maxUsec] for parsers that refuse anything above UINT32_MAX microseconds (and so `infinity`). */
val UINT32_MAX_USEC: BigInteger = UINT32_MAX_BIG

// ---------------------------------------------------------------------------------------------------
// parse_size

// strtoull(base 10) digits with an optional '+', then an optional '.' with optional digits: "3." and
// "10.M" are legal (parse-util.c "Ignore failure. E.g. 10.M is valid"); ".5" is not.
private const val SIZE_NUMBER_RE = """\+?[0-9]+(?:\.[0-9]*)?"""
private val SIZE_NUMBER = Labeled(Role.LITERAL, RegexTerminal(SIZE_NUMBER_RE, SIZE_NUMBER_RE))

private val SIZE_SUFFIXES = listOf("E", "P", "T", "G", "M", "K", "B")

private fun sizeChain(allowInnerWhitespace: Boolean): Combinator {
  val parts = mutableListOf<Combinator>()
  for (suffix in SIZE_SUFFIXES) {
    val element = if (allowInnerWhitespace) {
      SequenceCombinator(SIZE_NUMBER, OPT_WS, LiteralChoiceTerminal(suffix), OPT_WS)
    } else {
      SequenceCombinator(SIZE_NUMBER, LiteralChoiceTerminal(suffix))
    }
    parts.add(ZeroOrOne(element))
  }
  // The "" entry of the table: a bare number, which can only be the last element.
  parts.add(ZeroOrOne(SIZE_NUMBER))
  return SequenceCombinator(*parts.toTypedArray())
}

private val SIZE_EVAL_ELEMENT = Regex("""\s*\+?([0-9]+)(?:\.([0-9]*))?\s*([EPTGMKB]?)\s*""")

/** parse_size(text, base) as a byte count, or null if [text] is not evaluable. */
fun evaluateParseSize(text: String, base: Int): BigInteger? {
  val factorBase = BigInteger.valueOf(base.toLong())
  var pos = 0
  var total = BigInteger.ZERO
  while (pos < text.length) {
    val m = SIZE_EVAL_ELEMENT.matchAt(text, pos) ?: return null
    if (m.value.isEmpty()) return null
    pos += m.value.length
    val power = when (m.groupValues[3]) {
      "E" -> 6; "P" -> 5; "T" -> 4; "G" -> 3; "M" -> 2; "K" -> 1; else -> 0
    }
    val factor = factorBase.pow(power)
    total += BigInteger(m.groupValues[1]) * factor
    val frac = m.groupValues[2]
    if (frac.isNotEmpty()) total += BigDecimal("0.$frac").multiply(BigDecimal(factor)).toBigInteger()
  }
  return total
}

/**
 * A parse_size() value (https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L157-L283):
 * one or more `number[.fraction][suffix]` elements whose suffixes strictly descend through
 * `E P T G M K B ""` — `4M 11.5K`, `3.5G3B` and `4T3G3B` are legal; `3B3.5G`, `12P12P`, `12X`, `5k`
 * (suffixes are upper-case only) and `-1` are not (test-parse-util.c#L108, #L178). A bare number (the
 * `""` entry) must be the last element.
 *
 * @param base 1000 (SI, for rates) or 1024 (IEC, for sizes); only affects the range check.
 * @param minInclusive / maxInclusive bounds on the resulting byte (or bit) count. The upper bound
 *   defaults to ULLONG_MAX, above which parse_size() itself returns ERANGE.
 * @param allowInnerWhitespace false for an element of a whitespace-separated list (each word is
 *   parsed on its own, so a space would split the size into two words).
 */
fun parseSize(
  base: Int,
  minInclusive: BigInteger = BigInteger.ZERO,
  maxInclusive: BigInteger = UINT64_MAX_BIG,
  allowInnerWhitespace: Boolean = true,
): Combinator =
  ValueChecked(sizeChain(allowInnerWhitespace)) { text ->
    evaluateParseSize(text, base)?.let { it >= minInclusive && it <= maxInclusive }
  }

/** UINT32_MAX, the cap several tc size parsers apply after parse_size(). */
val SIZE_UINT32_MAX: BigInteger = UINT32_MAX_BIG

// ---------------------------------------------------------------------------------------------------
// parse_permyriad / parse_tc_percent

// The integer part goes through safe_atoi() (base 0, mangle_base), so `0x10%` is 16% and `0b 1%` is 1%,
// and parse_parts_value_*() return ERANGE when it is negative (`-0` is fine, and so is `0b-0`). Its
// magnitude is capped by the 100% check below, so int's range on the terminal itself is enough. The
// whole value reaches safe_atoi() up to the '.' or the symbol, so whitespace can reach the parser.
// https://github.com/systemd/systemd/blob/0f0682b773/src/basic/percent-util.c#L7-L23
private val PCT_INT = SafeAtoTerminal(
  signed = true,
  base = 0,
  flags = emptySet(),
  minInclusive = 0L,
  maxInclusive = Int.MAX_VALUE.toLong(),
  acceptsWhitespace = true,
  acceptsMinus = true,
)

/**
 * A percentage as parse_permyriad() reads it
 * (https://github.com/systemd/systemd/blob/0f0682b773/src/basic/percent-util.c#L135-L157): an integer
 * followed by `%` (up to two decimals), `‰` (up to one decimal) or `‱` (no decimals), at most 100%.
 * test-percent-util.c#L87 asserts `55.53%`, `555.5‰` and `1000‱` legal and `3.212%`, `3.22‰`,
 * `3.2‱`, `107%`, `-7%` and a bare `50` illegal.
 */
val TC_PERCENT: Combinator = ValueChecked(
  AlternativeCombinator(
    SequenceCombinator(PCT_INT, ZeroOrOne(SequenceCombinator(DOT, RegexTerminal("[0-9]{1,2}", "[0-9]{1,2}"))), LiteralChoiceTerminal("%")),
    SequenceCombinator(PCT_INT, ZeroOrOne(SequenceCombinator(DOT, RegexTerminal("[0-9]", "[0-9]"))), LiteralChoiceTerminal("‰")),
    SequenceCombinator(PCT_INT, LiteralChoiceTerminal("‱")),
  ),
) { text -> evaluatePermyriad(text)?.let { it in 0..10_000 } }

private val PCT_EVAL = Regex("""(.*?)(?:\.([0-9]+))?([%‰‱])""")

/** parse_permyriad_unbounded(text), or null if not evaluable (the integer part is read like [PCT_INT]). */
fun evaluatePermyriad(text: String): Long? {
  val m = PCT_EVAL.matchEntire(text) ?: return null
  val n = PCT_INT.valueOf(m.groupValues[1])?.toLong() ?: return null
  val frac = m.groupValues[2]
  return when (m.groupValues[3]) {
    "%" -> n * 100 + (frac.padEnd(2, '0').toLongOrNull() ?: 0)
    "‰" -> n * 10 + (frac.toLongOrNull() ?: 0)
    else -> n
  }
}
