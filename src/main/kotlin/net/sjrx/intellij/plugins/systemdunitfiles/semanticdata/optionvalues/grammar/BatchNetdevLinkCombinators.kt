package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import java.math.BigInteger

/*
 * Shared grammars for the systemd-networkd .netdev (src/network/netdev/) and udev .link (src/udev/net/)
 * validators added in the 2026-10 netdev-link batch.
 *
 * Everything here is pinned to systemd 0f0682b773, the commit recorded in
 * systemd-build/build/last_commit_hash. Browse it at https://github.com/systemd/systemd/blob/0f0682b773.
 *
 * These live in their own file (rather than Combinators.kt) only because several batches were written
 * in parallel. Several are general-purpose and are good candidates for promotion — see the batch's
 * RECOMMENDATIONS.md.
 */

// ---------------------------------------------------------------------------------------------------
// safe_atou() and friends — src/basic/parse-util.c (safe_atou_full, mangle_base).

/**
 * An unsigned number exactly as systemd's `safe_atou*()` helpers read it with base 0.
 *
 * Compared with [UnsignedNumberTerminal] this also accepts the two spellings that helper family adds
 * on top of strtoul(): the Python-style `0b`/`0o` prefixes that mangle_base() strips before calling
 * strtoul (so `0b101` and `0o17` are legal), and a leading `+`, which strtoul accepts and
 * safe_atou_full only refuses when SAFE_ATO_REFUSE_PLUS_MINUS is set (none of the callers here set it).
 *
 * Hex (`0x`/`0X`) and C octal (leading `0`) are accepted as for [UnsignedNumberTerminal]; a digit
 * string like `089` is number-shaped but not a valid octal literal, so it matches and is then rejected
 * — which is what strtoul()+`*x != 0` does too.
 *
 * Not accepted: `-0`. safe_atou_full lets a negative zero through (`l != 0 && s[0] == '-'` is the only
 * minus check), but no real configuration spells zero that way and accepting a leading `-` would blur
 * the error on every genuinely negative value.
 *
 * @param minInclusive lowest accepted value
 * @param maxExclusive one past the highest accepted value
 */
class SafeAtouTerminal(
  private val minInclusive: Long,
  private val maxExclusive: Long,
) : TerminalCombinator {

  private fun span(value: String, offset: Int): String? = SHAPE.matchAt(value, offset)?.value

  /** The numeric value of [text], or null when it is not a valid literal in its base. */
  private fun valueOf(text: String): BigInteger? {
    // mangle_base() only recognises 0b/0o at the very start, so they never carry a sign.
    if (text.length > 2 && (text.startsWith("0b") || text.startsWith("0B"))) return text.drop(2).toBigIntegerOrNull(2)
    if (text.length > 2 && (text.startsWith("0o") || text.startsWith("0O"))) return text.drop(2).toBigIntegerOrNull(8)
    val digits = text.removePrefix("+")
    return when {
      digits.length > 2 && (digits.startsWith("0x") || digits.startsWith("0X")) -> digits.drop(2).toBigIntegerOrNull(16)
      digits.length > 1 && digits.startsWith("0") -> digits.drop(1).toBigIntegerOrNull(8)
      else -> digits.toBigIntegerOrNull()
    }
  }

  private fun inRange(text: String): Boolean =
    valueOf(text)?.let { it >= BigInteger.valueOf(minInclusive) && it < BigInteger.valueOf(maxExclusive) } ?: false

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
    // 0b/0o first: as plain digits they would otherwise be read as "0" followed by junk. Within the
    // signed group, hex before octal before decimal for the same reason.
    val SHAPE = Regex("""0[bB][01]+|0[oO][0-7]+|\+?(?:0[xX][0-9a-fA-F]+|0[0-7]+|[0-9]+)""")
  }
}

/** Shorthand for a [SafeAtouTerminal] over `[minInclusive, maxExclusive)`. */
fun safeAtou(maxExclusive: Long, minInclusive: Long = 0L): Combinator = SafeAtouTerminal(minInclusive, maxExclusive)

/** Exclusive upper bound of a uint16_t. */
const val UINT16_LIMIT = 65_536L

/** Exclusive upper bound of a uint32_t / unsigned. */
const val UINT32_LIMIT = 4_294_967_296L

/**
 * `N` or `N-M` as parse_range() (src/basic/parse-util.c) reads it: the value is split on the first
 * `-` (EXTRACT_DONT_COALESCE_SEPARATORS, so `-5` and `5-` both fail) and each side goes through
 * safe_atou(). The `lower <= upper` checks that callers make afterwards compare parsed numbers and are
 * not modelled.
 */
fun parseRange(bound: Combinator): Combinator = SequenceCombinator(bound, ZeroOrOne(SequenceCombinator(HYPHEN, bound)))

/**
 * [inner] optionally wrapped in a matching pair of `"…"` or `'…'`, the form a word takes after
 * extract_first_word(…, EXTRACT_UNQUOTE) has removed its quotes (systemd's own test-cpu-set-util.c
 * writes `8 '9' 10 "11"`). Only whole-word quoting is modelled; quotes that start mid-word are not.
 */
fun optionallyQuoted(inner: Combinator): Combinator = AlternativeCombinator(
  SequenceCombinator(LiteralChoiceTerminal("\""), inner, LiteralChoiceTerminal("\"")),
  SequenceCombinator(LiteralChoiceTerminal("'"), inner, LiteralChoiceTerminal("'")),
  inner,
)


// ---------------------------------------------------------------------------------------------------
// CPU sets — config_parse_cpu_set (src/shared/cpu-set-util.c).

/** The largest CPU index cpu_set_add_range() accepts is CPU_SET_MAX_NCPU - 1 = 8191. */
private const val CPU_SET_MAX_NCPU = 8192L

// One extract_first_word() separator run: any mix of whitespace and commas (separators coalesce).
private val CPU_SET_SEPARATOR = RegexTerminal("""[\s,]+""", """[\s,]+""")

private val CPU_SET_ITEM = optionallyQuoted(parseRange(safeAtou(CPU_SET_MAX_NCPU)))

/**
 * A CPU set: CPU indices and `N-M` ranges separated by whitespace and/or commas.
 *
 * config_parse_cpu_set splits with extract_first_word(…, WHITESPACE ",", EXTRACT_UNQUOTE), so separators
 * coalesce, leading and trailing separators are harmless (`0, 1, 2, 3, 4, 5, 6, 7, 63, ` is in
 * test-cpu-set-util.c), and a word may be quoted. Each word goes to parse_range(), whose bounds are
 * safe_atou() numbers — so hex works — and cpu_set_add_range() refuses anything at or above
 * CPU_SET_MAX_NCPU (8192). A range whose lower end exceeds its upper end (`3-0`) is rejected by systemd
 * after parsing; that compares numbers and is not modelled.
 */
val CPU_SET: Combinator = SequenceCombinator(
  ZeroOrOne(CPU_SET_SEPARATOR),
  CPU_SET_ITEM,
  ZeroOrMore(SequenceCombinator(CPU_SET_SEPARATOR, CPU_SET_ITEM)),
  ZeroOrOne(CPU_SET_SEPARATOR),
)


// ---------------------------------------------------------------------------------------------------
// Sizes — parse_size() (src/basic/parse-util.c).

// strtoull(…, 10) after parse_size has skipped whitespace: an optional `+` (a `-` is refused), digits,
// then an optional `.` and fraction. parse_size steps over the `.` unconditionally and ignores a
// missing fraction ("E.g. 10.M is valid"), so `10.` and `10.M` are both legal.
private val SIZE_NUMBER = RegexTerminal("""\+?[0-9]+(?:\.[0-9]*)?""", """\+?[0-9]+(?:\.[0-9]*)?""")

/**
 * A parse_size() value with base 1000 (the SI table: `E`, `P`, `T`, `G`, `M`, `K`, `B` and bare).
 *
 * parse_size accepts a run of `number[suffix]` elements whose suffixes strictly descend through the
 * table (`start_pos = i + 1`), with optional whitespace before each number and between a number and its
 * suffix. The bare (suffix-less) element is the last table entry, so it can only come last. That
 * ordering is spelled out structurally: [sizeChain] for table index `i` offers every suffix from `i`
 * onwards, each optionally followed by a chain starting after it.
 *
 * Suffixes are case-sensitive: `1k` is not a size.
 */
fun siSize(): Combinator = sizeChain(listOf("E", "P", "T", "G", "M", "K", "B"))

private fun sizeChain(suffixes: List<String>): Combinator {
  val ws = ZeroOrOne(WhitespaceTerminal())
  // Built from the last suffix backwards so each chain reuses the shorter one after it.
  var chainAfter: Combinator? = null
  val chains = ArrayList<Combinator>()
  // The bare element (no suffix) is always the final alternative of every chain.
  val bare: Combinator = SequenceCombinator(ws, SIZE_NUMBER)
  for (i in suffixes.indices.reversed()) {
    val element = SequenceCombinator(ws, SIZE_NUMBER, ws, LiteralChoiceTerminal(suffixes[i]))
    val withRest = if (chainAfter == null) SequenceCombinator(element, ZeroOrOne(bare))
    else SequenceCombinator(element, ZeroOrOne(chainAfter))
    chains.add(0, withRest)
    chainAfter = AlternativeCombinator(*chains.toTypedArray(), bare)
  }
  return chainAfter ?: bare
}


// ---------------------------------------------------------------------------------------------------
// Durations — parse_nsec() (src/basic/time-util.c).

// strtoll(…, 10) as parse_nsec drives it: an optional `+` (a leading `-` is refused), then digits with
// an optional fraction — or a bare fraction like `.5`, which parse_nsec accepts because it checks for
// `.` before it checks whether strtoll consumed anything. A trailing `.` with no digits (`3.`) is refused.
private val DURATION_NUMBER = RegexTerminal("""\+?[0-9]+(?:\.[0-9]+)?|\.[0-9]+""", """\+?[0-9]+(?:\.[0-9]+)?|\.[0-9]+""")

/** extract_nsec_multiplier's table (minus its empty default entry, which is the bare-number case). */
private val NSEC_SUFFIX = LiteralChoiceTerminal(
  "seconds", "second", "sec", "s", "minutes", "minute", "min", "months", "month", "M", "msec", "ms", "m",
  "hours", "hour", "hr", "h", "days", "day", "d", "weeks", "week", "w", "years", "year", "y",
  "usec", "us", "μs", "µs", "nsec", "ns",
)

/**
 * A parse_nsec() duration WITHOUT the `infinity` spelling: one or more `number[unit]` elements, an
 * element without a unit meaning nanoseconds. Whitespace may separate elements and may sit between a
 * number and its unit; an element with a unit may be followed directly by the next one (`1s500ms`).
 *
 * Lenient on one corner: parse_nsec refuses a bare number followed immediately by a `.` fraction
 * (`12.34.56`), which this grammar would read as `12.34` + `.56`.
 */
val NSEC_DURATION_FINITE: Combinator = run {
  val ws = ZeroOrOne(WhitespaceTerminal())
  val element = AlternativeCombinator(
    SequenceCombinator(DURATION_NUMBER, ws, NSEC_SUFFIX),
    DURATION_NUMBER,
  )
  SequenceCombinator(element, ZeroOrMore(SequenceCombinator(ws, element)))
}


// ---------------------------------------------------------------------------------------------------
// Paths.

// Rejects a `..` component anywhere (see NO_DOT_DOT in Combinators.kt).
private const val NO_DOT_DOT_COMPONENT = """(?!(?:[\s\S]*/)?\.\.(?:/|$))"""

/**
 * An absolute path checked by path_simplify_and_warn(…, PATH_CHECK_ABSOLUTE|PATH_CHECK_NON_API_VFS)
 * (src/shared/parse-helpers.c), taken verbatim — no specifier expansion, no unquoting — as the netdev
 * `*KeyFile=` settings do. After path_simplify() the path must be normalized (so no `..` component) and
 * must not be at or below one of the API VFS mounts `/dev`, `/sys`, `/proc` (path_below_api_vfs, which
 * compares whole components, so `/devices` is fine). Spaces are ordinary path characters.
 */
val NON_API_VFS_ABSOLUTE_PATH = RegexTerminal(
  """[\s\S]+""",
  """(?!/+(?:dev|sys|proc)(?:/|$))$NO_DOT_DOT_COMPONENT/[\s\S]*""",
)

/**
 * An absolute path accepted by path_is_absolute() && path_is_safe() (src/basic/path-util.h): it starts
 * with `/` and has no `..` component. Taken verbatim.
 */
val SAFE_ABSOLUTE_PATH = RegexTerminal(
  """/[\s\S]*""",
  """$NO_DOT_DOT_COMPONENT/[\s\S]*""",
)


// ---------------------------------------------------------------------------------------------------
// WireGuard keys and credentials — wireguard_decode_key_and_warn (src/network/netdev/wireguard.c) →
// unbase64mem_full (src/basic/hexdecoct.c).

/**
 * A 32-byte WireGuard key in base64, as unbase64mem_full() decodes it.
 *
 * 32 bytes is ten full 4-character groups plus a final 3-character group padded with one `=`; the
 * padding is mandatory — without it unbase64_next() runs out of input mid-group and fails. The third
 * character of a padded group must have its low two bits clear (`c & 3`), which limits the 43rd
 * character to `AEIMQUYcgkosw048`. unbase64char() accepts the URL-safe alphabet too (`-` for `+`,
 * `_` for `/`), and unbase64_next() skips whitespace anywhere in the input.
 */
val WIREGUARD_BASE64_KEY = RegexTerminal(
  """[A-Za-z0-9+/_=\-]+(?:[ \t]+[A-Za-z0-9+/_=\-]+)*""",
  """(?:[A-Za-z0-9+/_\-][ \t]*){42}[AEIMQUYcgkosw048][ \t]*=""",
)

/**
 * `@name` — a credential reference resolved by read_credential(), which refuses any name that fails
 * credential_name_valid() (src/shared/creds-util.c). [CONDITION_CREDENTIAL] models that check.
 */
val CREDENTIAL_REFERENCE: Combinator = SequenceCombinator(LiteralChoiceTerminal("@"), CONDITION_CREDENTIAL)


// ---------------------------------------------------------------------------------------------------
// Route tables — manager_get_route_table_from_string (src/network/networkd-route-util.c).

// Names an administrator can define with RouteTable=name:number in networkd.conf. That parser splits
// words on whitespace, cuts each at the first `:`, and refuses an empty or all-digit name; any other
// spelling is a potential name, so a non-numeric word here cannot be checked against a closed set.
private val ROUTE_TABLE_NAME = RegexTerminal("""[^\s:]+""", """(?![0-9]+$)[^\s:]+""")

/**
 * A route table: one of the predefined names `default`, `main`, `local`, a name defined in
 * networkd.conf, or a non-zero uint32 (safe_atou32, so hex works). Each branch carries its own EOF so
 * the classic first-match engine can fall back from the numeric branch to the name branch on `1abc`.
 */
val ROUTE_TABLE: Combinator = AlternativeCombinator(
  SequenceCombinator(FlexibleLiteralChoiceTerminal("default", "main", "local"), EOF()),
  SequenceCombinator(safeAtou(UINT32_LIMIT, 1), EOF()),
  SequenceCombinator(ROUTE_TABLE_NAME, EOF()),
)
