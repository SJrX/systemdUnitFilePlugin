package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import java.math.BigInteger

/*
 * Shared grammars for the unit-core validator batch ([Timer] timers, [Socket] Listen*=, assorted
 * [Unit]/[Service] settings and the generic long / iec_size_long / path parsers).
 *
 * Everything here is modelled on systemd 0f0682b773, the commit recorded in
 * systemd-build/build/last_commit_hash: https://github.com/systemd/systemd/blob/0f0682b773
 *
 * These were written for one batch and live in their own file so parallel work on Combinators.kt does
 * not collide; several of them (the time span, the unit-specifier escape hatch, the safe_ato* number)
 * are general enough to be promoted there.
 *
 * Every grammar here is careful about the classic engine: no repetition wraps something that can match
 * the empty string (classic ZeroOrMore/OneOrMore would loop forever), and every alternative that
 * could stop on a prefix of another is either ordered after it or terminated with EOF().
 */

// ---------------------------------------------------------------------------------------------------
// Unit specifiers — specifier_printf (src/shared/specifier.c) driven by the table in
// unit_full_printf_full (src/core/unit-printf.c), which also pulls in COMMON_SYSTEM_SPECIFIERS,
// COMMON_CREDS_SPECIFIERS and COMMON_TMP_SPECIFIERS (src/shared/specifier.h).
//
// `%%` is a literal percent sign. `%` followed by a letter or digit that is NOT in the table is a hard
// error (-EBADSLT, "Failed to resolve unit specifiers"), so the setting is ignored. `%` followed by
// anything else (or at the very end) is copied through verbatim.

/** Every specifier letter unit_full_printf() knows about. */
const val UNIT_SPECIFIER_LETTERS = "aAbBcCdDEfgGhHiIjJlLmMnNopPqrRsStTuUvVwWyY"

/** One character of specifier-expanded text: anything but a `%`, or a `%` that expands to something. */
const val UNIT_TEXT_CHAR = """(?:[^%]|%%|%[$UNIT_SPECIFIER_LETTERS]|%(?![A-Za-z0-9%]))"""

// A run of text that contains at least one real (value-changing) specifier, and no unknown ones.
private const val NON_SPECIFIER_CHAR = """(?:[^%]|%%|%(?![A-Za-z0-9%]))"""

/**
 * A whole value that contains at least one unit specifier (`%i`, `%t`, …) and no unknown ones.
 *
 * What a specifier expands to is only known when the unit is loaded, so a value that uses one cannot
 * be checked against a grammar for the expanded text. This terminal is the escape hatch: it accepts
 * such a value as long as every `%X` in it is one systemd knows (an unknown one makes systemd ignore
 * the whole assignment, so that is still flagged).
 */
val UNIT_SPECIFIED_VALUE = RegexTerminal(
  """[\s\S]*%[A-Za-z0-9][\s\S]*""",
  """(?=$NON_SPECIFIER_CHAR*%[$UNIT_SPECIFIER_LETTERS])$UNIT_TEXT_CHAR*"""
)

/**
 * [grammar] (which must itself end in [EOF]), or any value that uses a unit specifier — for settings
 * whose C parser runs unit_full_printf()/unit_path_printf() before parsing the expanded text.
 */
fun withUnitSpecifiers(grammar: Combinator): Combinator =
  AlternativeCombinator(grammar, SequenceCombinator(UNIT_SPECIFIED_VALUE, EOF()))


// ---------------------------------------------------------------------------------------------------
// Time spans — parse_time(…, USEC_PER_SEC) a.k.a. parse_sec (src/basic/time-util.c).
//
//  - Leading/trailing whitespace is skipped. `infinity` stands alone (only whitespace may follow).
//  - Each element is a number read by strtoll(…, 10) — so an optional `+`, never `-` — with an
//    optional `.digits` fraction (a bare `.5` is fine, `5.` is not), then optional whitespace and an
//    optional unit from extract_multiplier's table. With no unit the default (seconds) applies.
//  - Elements may follow each other directly after a unit (`1h30min`, `3.1s.2`) but a number without
//    a unit must be followed by whitespace or the end (`12.34.56`, `5+5` are refused).
//  - A unit is matched by prefix in table order, so `5mins` reads `min` and then chokes on `s`.
//
// Overflow (ERANGE past UINT64_MAX µs) is not modelled.

private val TS_OPTIONAL_WS = ZeroOrOne(WhitespaceTerminal())

/** One number of a time span, as strtoll(…, 10) plus parse_time's own `.fraction` handling reads it. */
val TIMESPAN_NUMBER = RegexTerminal("""\+?[0-9]+(?:\.[0-9]+)?|\.[0-9]+""", """\+?[0-9]+(?:\.[0-9]+)?|\.[0-9]+""")

/** extract_multiplier's suffix table (case-sensitive: `m` is minutes, `M` is months). */
val TIMESPAN_UNIT = FlexibleLiteralChoiceTerminal(
  "seconds", "second", "sec", "s",
  "minutes", "minute", "min",
  "months", "month", "M",
  "msec", "ms", "m",
  "hours", "hour", "hr", "h",
  "days", "day", "d",
  "weeks", "week", "w",
  "years", "year", "y",
  "usec", "us", "μs", "µs",
)

private val TIMESPAN_ELEMENT = AlternativeCombinator(
  SequenceCombinator(TIMESPAN_NUMBER, TS_OPTIONAL_WS, TIMESPAN_UNIT, TS_OPTIONAL_WS),
  SequenceCombinator(TIMESPAN_NUMBER, AlternativeCombinator(WhitespaceTerminal(), EOF())),
)

/** A complete parse_sec() time span, `infinity` included, ending at [EOF]. */
val TIMESPAN = AlternativeCombinator(
  SequenceCombinator(TS_OPTIONAL_WS, LiteralChoiceTerminal("infinity"), TS_OPTIONAL_WS, EOF()),
  SequenceCombinator(TS_OPTIONAL_WS, OneOrMore(TIMESPAN_ELEMENT), EOF()),
)


// ---------------------------------------------------------------------------------------------------
// Calendar events — calendar_spec_from_string (src/shared/calendarspec.c).
//
//   [WEEKDAYS] [DATE] [TIME] [TIMEZONE]      or      KEYWORD [TIMEZONE]
//
// What is checked: the shape the C parser walks (weekday list with `..`/`-` ranges and an optional
// trailing comma, a two- or three-part date with `-` or `~`, a two- or three-part time, `@epoch`), and
// the per-field ranges calendar_spec_valid() enforces after fix_year() (years 0..99 or 1970..2199,
// months 1..12, days 1..31 or 1..28 counted from the end of the month, hours 0..23, minutes and
// seconds 0..59), with repetitions bounded by `to - from` and never zero.
//
// What is deliberately left lenient: cross-field checks (reversed ranges like `12..1`, a repetition
// that overshoots the field like `20/4` hours, descending weekday ranges like `Fri..Mon`), rounding
// of second fractions, and the timezone, which systemd only accepts if it names a file under
// /usr/share/zoneinfo or the host's local zone abbreviation — any word shaped like a zone name is
// accepted here.

private val CAL_SPACES = RegexTerminal(" +", " +")
private val CAL_STAR = LiteralChoiceTerminal("*")
private val CAL_COMMA = LiteralChoiceTerminal(",")
private val CAL_RANGE = LiteralChoiceTerminal("..")
private val CAL_REPEAT = LiteralChoiceTerminal("/")
private val CAL_TILDE = LiteralChoiceTerminal("~")
/**
 * A plain decimal number in `[minInclusive, maxExclusive)`, as calendarspec's parse_component_decimal
 * reads one: it insists on a leading digit (so no sign, unlike [IntegerTerminal], which accepts `-0`)
 * and then calls strtoul(…, 10).
 */
class DecimalNumberTerminal(private val minInclusive: Long, private val maxExclusive: Long) : TerminalCombinator {

  private fun span(value: String, offset: Int): String? = SHAPE.matchAt(value, offset)?.value

  private fun inRange(text: String): Boolean =
    text.toBigInteger().let { it >= BigInteger.valueOf(minInclusive) && it < BigInteger.valueOf(maxExclusive) }

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

  override fun toString(): String = "Decimal($minInclusive,$maxExclusive)"

  private companion object {
    val SHAPE = Regex("[0-9]+")
  }
}

private val CAL_FRACTION = SequenceCombinator(LiteralChoiceTerminal("."), RegexTerminal("[0-9]+", "[0-9]+"))

/** One chain element: `value[..value][/repeat]` — prepend_component. */
private fun calComponent(value: Combinator, repeat: Combinator): Combinator = SequenceCombinator(
  value,
  ZeroOrOne(SequenceCombinator(CAL_RANGE, value)),
  ZeroOrOne(SequenceCombinator(CAL_REPEAT, repeat)),
)

/** `*`, or a comma-separated list of components — parse_chain. `*` never combines with a list. */
private fun calChain(value: Combinator, repeat: Combinator): Combinator {
  val component = calComponent(value, repeat)
  return AlternativeCombinator(CAL_STAR, SequenceCombinator(component, ZeroOrMore(SequenceCombinator(CAL_COMMA, component))))
}

// fix_year() maps 0..69 to 2000..2069 and 70..99 to 1970..1999 before the 1970..2199 range check.
private val CAL_YEAR = AlternativeCombinator(DecimalNumberTerminal(1970, 2200), DecimalNumberTerminal(0, 100))
private val CAL_YEAR_CHAIN = calChain(CAL_YEAR, DecimalNumberTerminal(1, 230))
private val CAL_MONTH_CHAIN = calChain(DecimalNumberTerminal(1, 13), DecimalNumberTerminal(1, 12))
private val CAL_DAY_CHAIN = calChain(DecimalNumberTerminal(1, 32), DecimalNumberTerminal(1, 31))
// With `~` the day counts back from the end of the month and chain_valid() lowers the ceiling to 28.
private val CAL_END_OF_MONTH_DAY_CHAIN = calChain(DecimalNumberTerminal(1, 29), DecimalNumberTerminal(1, 28))
private val CAL_HOUR_CHAIN = calChain(DecimalNumberTerminal(0, 24), DecimalNumberTerminal(1, 24))
private val CAL_MINUTE_CHAIN = calChain(DecimalNumberTerminal(0, 60), DecimalNumberTerminal(1, 60))
// Seconds are parsed with usec = true: a `.fraction` is allowed on values and repetitions alike.
private val CAL_SECOND_VALUE = SequenceCombinator(DecimalNumberTerminal(0, 60), ZeroOrOne(CAL_FRACTION))
private val CAL_SECOND_REPEAT = AlternativeCombinator(
  SequenceCombinator(DecimalNumberTerminal(0, 60), CAL_FRACTION),
  DecimalNumberTerminal(1, 60),
)
private val CAL_SECOND_CHAIN = calChain(CAL_SECOND_VALUE, CAL_SECOND_REPEAT)

private val CAL_DAY_PART = AlternativeCombinator(
  SequenceCombinator(HYPHEN, CAL_DAY_CHAIN),
  SequenceCombinator(CAL_TILDE, CAL_END_OF_MONTH_DAY_CHAIN),
)

/** `[YEAR-]MONTH-DAY` or `[YEAR-]MONTH~DAY` — parse_date. A lone component is not a date. */
private val CAL_DATE = AlternativeCombinator(
  SequenceCombinator(CAL_YEAR_CHAIN, HYPHEN, CAL_MONTH_CHAIN, CAL_DAY_PART),
  SequenceCombinator(CAL_MONTH_CHAIN, CAL_DAY_PART),
)

/** `HOUR:MINUTE[:SECOND]` — parse_calendar_time. */
private val CAL_TIME = SequenceCombinator(
  CAL_HOUR_CHAIN, COLON, CAL_MINUTE_CHAIN,
  ZeroOrOne(SequenceCombinator(COLON, CAL_SECOND_CHAIN)),
)

// `@SECONDS` since the epoch; the resulting year must still pass the 1970..2199 check, i.e. the value
// must be below 2200-01-01T00:00:00Z.
private val CAL_EPOCH = SequenceCombinator(LiteralChoiceTerminal("@"), DecimalNumberTerminal(0L, 7_258_118_400L))

private val CAL_DATE_AND_TIME = AlternativeCombinator(
  CAL_EPOCH,
  SequenceCombinator(CAL_DATE, ZeroOrOne(SequenceCombinator(CAL_SPACES, CAL_TIME))),
  CAL_TIME,
)

/** parse_weekdays' table; startswith_no_case, so any casing. */
val CALENDAR_WEEKDAY = FlexibleLiteralChoiceTerminal(
  "Monday", "Mon", "Tuesday", "Tue", "Wednesday", "Wed", "Thursday", "Thu",
  "Friday", "Fri", "Saturday", "Sat", "Sunday", "Sun",
  ignoreCase = true,
)

private val CAL_WEEKDAY_ITEM = SequenceCombinator(
  CALENDAR_WEEKDAY,
  ZeroOrOne(SequenceCombinator(LiteralChoiceTerminal("..", "-"), CALENDAR_WEEKDAY)),
)

// A trailing comma is allowed ("Wednesday,", "Wed, 17:48"), an open range is not ("Monday..").
private val CAL_WEEKDAYS = SequenceCombinator(
  CAL_WEEKDAY_ITEM,
  ZeroOrMore(SequenceCombinator(CAL_COMMA, CAL_WEEKDAY_ITEM)),
  ZeroOrOne(CAL_COMMA),
)

private val CAL_BODY = AlternativeCombinator(
  SequenceCombinator(CAL_WEEKDAYS, ZeroOrOne(SequenceCombinator(CAL_SPACES, CAL_DATE_AND_TIME))),
  CAL_DATE_AND_TIME,
)

/** The shorthand keywords, matched with strcaseeq. `anually` is a kept-for-compatibility misspelling. */
val CALENDAR_KEYWORD = FlexibleLiteralChoiceTerminal(
  "minutely", "hourly", "daily", "monthly", "weekly", "yearly", "annually", "anually", "quarterly",
  "biannually", "bi-annually", "semiannually", "semi-annually",
  ignoreCase = true,
)

/**
 * A timezone suffix: ` UTC` (any case), the host's local zone abbreviation, or an IANA zone name
 * (verify_timezone's character set: letters, digits, `-_+/`, no empty components). Whether the zone
 * actually exists depends on the machine, so only the shape is checked; a leading letter keeps a date
 * or time from being mistaken for one.
 */
private val CAL_TIMEZONE = RegexTerminal(
  """[A-Za-z][A-Za-z0-9_+\-]*(?:/[A-Za-z0-9_+\-]+)*""",
  """[A-Za-z][A-Za-z0-9_+\-]*(?:/[A-Za-z0-9_+\-]+)*"""
)
private val CAL_OPTIONAL_TIMEZONE = ZeroOrOne(SequenceCombinator(CAL_SPACES, CAL_TIMEZONE))

/** A complete calendar event expression, ending at [EOF]. */
val CALENDAR_SPEC = AlternativeCombinator(
  SequenceCombinator(CALENDAR_KEYWORD, CAL_OPTIONAL_TIMEZONE, EOF()),
  SequenceCombinator(CAL_BODY, CAL_OPTIONAL_TIMEZONE, EOF()),
)


// ---------------------------------------------------------------------------------------------------
// Numbers as the safe_ato* family reads them (src/basic/parse-util.c): leading whitespace is skipped,
// mangle_base() turns a `0b`/`0o` prefix into base 2/8, and otherwise strtoul()/strtoll() run with base
// 0, so `0x` is hexadecimal and a leading `0` is octal. The unsigned helpers accept a leading `+` and
// refuse `-` (even `-0`); the signed ones accept both signs. `0b`/`0o` are only recognised unsigned.

/**
 * An integer spelled any way the safe_atou and safe_atoi helpers accept, range-checked on the value it denotes.
 *
 * Unlike [UnsignedNumberTerminal] this also accepts `0b…`, `0o…` and a leading `+`, and with
 * [signed] a leading `-`.
 */
class SafeAtoNumberTerminal(
  private val minInclusive: BigInteger,
  private val maxInclusive: BigInteger,
  private val signed: Boolean,
) : TerminalCombinator {

  constructor(minInclusive: Long, maxInclusive: Long, signed: Boolean) :
    this(BigInteger.valueOf(minInclusive), BigInteger.valueOf(maxInclusive), signed)

  private fun span(value: String, offset: Int): String? = SHAPE.matchAt(value, offset)?.value

  private fun valueOf(text: String): BigInteger? {
    var s = text
    if (s.length > 2 && (s.startsWith("0b") || s.startsWith("0B"))) return s.drop(2).toBigIntegerOrNull(2)
    if (s.length > 2 && (s.startsWith("0o") || s.startsWith("0O"))) return s.drop(2).toBigIntegerOrNull(8)
    var negative = false
    if (s.startsWith("+") || s.startsWith("-")) {
      negative = s[0] == '-'
      s = s.drop(1)
    }
    val magnitude = when {
      s.length > 2 && (s.startsWith("0x") || s.startsWith("0X")) -> s.drop(2).toBigIntegerOrNull(16)
      s.length > 1 && s.startsWith("0") -> s.drop(1).toBigIntegerOrNull(8)
      else -> s.toBigIntegerOrNull(10)
    } ?: return null
    if (negative && !signed) return null // safe_atou* refuse any '-', even "-0"
    return if (negative) magnitude.negate() else magnitude
  }

  private fun inRange(text: String): Boolean = valueOf(text)?.let { it >= minInclusive && it <= maxInclusive } ?: false

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

  override fun toString(): String = "SafeAto($minInclusive,$maxInclusive${if (signed) ",signed" else ""})"

  private companion object {
    // Prefixed forms first so "0x10" isn't read as "0". Over-long digit runs (e.g. "08", an invalid
    // octal) are still matched whole so the bad token can be highlighted.
    val SHAPE = Regex("""0[bB][0-9]+|0[oO][0-9]+|[+-]?(?:0[xX][0-9a-fA-F]+|[0-9]+)""")
  }
}

/** An unsigned number in `[minInclusive, maxInclusive]` as safe_atou*() reads it. */
fun safeAtou(minInclusive: Long, maxInclusive: Long): Combinator = SafeAtoNumberTerminal(minInclusive, maxInclusive, signed = false)


// ---------------------------------------------------------------------------------------------------
// Byte sizes — parse_size(…, 1024) (src/basic/parse-util.c).
//
// A run of `number[.fraction] [suffix]` elements, whitespace allowed before each element and between a
// number and its suffix. The number is read by strtoull(…, 10), so a leading `+` is fine and a `-` is
// ERANGE; the fraction may be empty (`10.M` is accepted). Suffixes are B, K, M, G, T, P, E (upper case
// only) or none, and an element without a suffix can only come last. systemd also insists suffixes
// appear in strictly descending order (`1G 512M`, not `512M 1G`) and that the sum fits — neither is
// modelled.

private val SIZE_NUMBER = RegexTerminal("""\+?[0-9]+(?:\.[0-9]*)?""", """\+?[0-9]+(?:\.[0-9]*)?""")
private val SIZE_SUFFIX = LiteralChoiceTerminal("B", "K", "M", "G", "T", "P", "E")

/** A complete parse_size(…, 1024) value, ending at [EOF]. */
val IEC_SIZE = SequenceCombinator(
  TS_OPTIONAL_WS,
  ZeroOrMore(SequenceCombinator(SIZE_NUMBER, TS_OPTIONAL_WS, SIZE_SUFFIX, TS_OPTIONAL_WS)),
  ZeroOrOne(SequenceCombinator(SIZE_NUMBER, TS_OPTIONAL_WS)),
  EOF(),
)


// ---------------------------------------------------------------------------------------------------
// Paths as path_simplify_and_warn() (src/shared/parse-helpers.c) checks them after specifier expansion:
// PATH_CHECK_ABSOLUTE, then path_simplify() (which drops `.` components and duplicate slashes), then
// path_is_normalized() (which refuses any `..` component), and with PATH_CHECK_NON_API_VFS also
// path_below_api_vfs() (refuses /dev, /sys, /proc and anything below them).

// Rejects a `..` component anywhere: the optional `(?:[\s\S]*/)` swallows any leading directories.
private const val NO_DOT_DOT_COMPONENT = """(?!(?:[\s\S]*/)?\.\.(?:/|$))"""

// path_startswith() is component-aware: /dev and /dev/x are API VFS, /devices is not.
private const val NOT_API_VFS = """(?!/+(?:dev|sys|proc)(?:/|$))"""

private const val SPECIFIER_OR_SLASH = """(?:/|%[$UNIT_SPECIFIER_LETTERS])"""

/** An absolute, normalized path after unit_path_printf(); may begin with a specifier such as `%t`. */
val UNIT_ABSOLUTE_PATH = RegexTerminal("""[\s\S]+""", """$NO_DOT_DOT_COMPONENT$SPECIFIER_OR_SLASH$UNIT_TEXT_CHAR*""")

/** As [UNIT_ABSOLUTE_PATH], but also not at or below /dev, /sys or /proc (PATH_CHECK_NON_API_VFS). */
val UNIT_ABSOLUTE_NON_API_VFS_PATH = RegexTerminal(
  """[\s\S]+""",
  """$NO_DOT_DOT_COMPONENT$NOT_API_VFS$SPECIFIER_OR_SLASH$UNIT_TEXT_CHAR*"""
)

/** An absolute, normalized path taken literally (no specifier expansion) — config_parse_path. */
val LITERAL_ABSOLUTE_PATH = RegexTerminal("""[\s\S]+""", """$NO_DOT_DOT_COMPONENT/[\s\S]*""")
