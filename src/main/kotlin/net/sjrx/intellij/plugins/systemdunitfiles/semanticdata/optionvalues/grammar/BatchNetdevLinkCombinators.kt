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
 * A leading `-` is number-shaped (so `-1` is reported as an out-of-range number rather than as junk)
 * and valid only when the value is zero: safe_atou_full's only minus check is `l != 0 && s[0] == '-'`,
 * so `-0` (and `-0x0`) really are accepted.
 *
 * @param minInclusive lowest accepted value
 * @param maxExclusive one past the highest accepted value
 * @param allowMinus false for a position where a `-` cannot reach safe_atou at all — the lower bound
 *   of a parse_range(), which is cut at the first `-`, so `-0` there is an empty word and EINVAL.
 */
class NetdevLinkSafeAtouTerminal(
  private val minInclusive: Long,
  private val maxExclusive: Long,
  private val allowMinus: Boolean = true,
) : TerminalCombinator {

  private fun span(value: String, offset: Int): String? = (if (allowMinus) SHAPE else SHAPE_NO_MINUS).matchAt(value, offset)?.value

  private fun inRange(text: String): Boolean = inRange(valueOf(text), minInclusive, maxExclusive)

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

  override fun toString(): String = "SafeAtou($minInclusive,$maxExclusive${if (allowMinus) "" else ",no-minus"})"

  internal companion object {
    // 0b/0o first: as plain digits they would otherwise be read as "0" followed by junk. Within the
    // signed group, hex before octal before decimal for the same reason.
    val SHAPE = Regex("""0[bB][01]+|0[oO][0-7]+|[+-]?(?:0[xX][0-9a-fA-F]+|0[0-7]+|[0-9]+)""")
    val SHAPE_NO_MINUS = Regex("""0[bB][01]+|0[oO][0-7]+|\+?(?:0[xX][0-9a-fA-F]+|0[0-7]+|[0-9]+)""")

    fun inRange(value: BigInteger?, minInclusive: Long, maxExclusive: Long): Boolean =
      value != null && value >= BigInteger.valueOf(minInclusive) && value < BigInteger.valueOf(maxExclusive)

    /**
     * What safe_atou() makes of the whole string [s], or null when it refuses it: leading whitespace is
     * skipped (`s += strspn(s, WHITESPACE)`), then the rest must be exactly one [SHAPE] literal.
     */
    fun safeAtouValue(s: String): BigInteger? {
      val text = s.trimStart(' ', '\t', '\n', '\r')
      return if (SHAPE.matches(text)) valueOf(text) else null
    }

    /** The numeric value of [text], or null when it is not a valid literal in its base. */
    fun valueOf(text: String): BigInteger? {
      // mangle_base() only recognises 0b/0o at the very start, so they never carry a sign.
      if (text.length > 2 && (text.startsWith("0b") || text.startsWith("0B"))) return text.drop(2).toBigIntegerOrNull(2)
      if (text.length > 2 && (text.startsWith("0o") || text.startsWith("0O"))) return text.drop(2).toBigIntegerOrNull(8)
      val digits = text.removePrefix("+").removePrefix("-")
      val magnitude = when {
        digits.length > 2 && (digits.startsWith("0x") || digits.startsWith("0X")) -> digits.drop(2).toBigIntegerOrNull(16)
        digits.length > 1 && digits.startsWith("0") -> digits.drop(1).toBigIntegerOrNull(8)
        else -> digits.toBigIntegerOrNull()
      }
      // strtoul() negates a `-` value; safe_atou_full then refuses it unless it is zero.
      return if (text.startsWith("-") && magnitude != BigInteger.ZERO) null else magnitude
    }
  }
}

/**
 * A [NetdevLinkSafeAtouTerminal] over `[minInclusive, maxExclusive)`, coloured as a literal. The [Labeled] wrapper
 * is only there because [defaultRole] does not know this terminal class yet (it is transparent to
 * matching); once the terminal is promoted next to [UnsignedNumberTerminal] it can go.
 */
fun netdevLinkSafeAtou(maxExclusive: Long, minInclusive: Long = 0L, allowMinus: Boolean = true): Combinator =
  Labeled(Role.LITERAL, NetdevLinkSafeAtouTerminal(minInclusive, maxExclusive, allowMinus))

/** Exclusive upper bound of a uint16_t. */
const val UINT16_LIMIT = 65_536L

/** Exclusive upper bound of a uint32_t / unsigned. */
const val NETDEV_LINK_UINT32_LIMIT = 4_294_967_296L

/**
 * `N` or `N-M` as parse_range() (src/basic/parse-util.c) reads it, each bound a safe_atou() number in
 * `[minInclusive, maxExclusive)`. The value is split on the first `-` (EXTRACT_DONT_COALESCE_SEPARATORS,
 * so `-5` and `5-` both fail); the lower part goes to safe_atou() and so can never start with `-`
 * (`-0` is an empty lower word), while the upper part is handed to safe_atou() as is, so `5--0` is legal.
 * safe_atou() skips leading whitespace, so whitespace may follow the dash — but only where the caller's
 * own word splitting cannot cut there, hence [spaceAfterDash] (true for a whole-value range such as
 * PortRange=; false inside a whitespace-separated list, where `0- 3` is two words). The `lower <= upper`
 * checks that callers make afterwards compare parsed numbers and are not modelled.
 */
fun parseRange(maxExclusive: Long, minInclusive: Long = 0L, spaceAfterDash: Boolean = false): Combinator = SequenceCombinator(
  netdevLinkSafeAtou(maxExclusive, minInclusive, allowMinus = false),
  ZeroOrOne(
    if (spaceAfterDash) SequenceCombinator(HYPHEN, ZeroOrOne(WhitespaceTerminal()), netdevLinkSafeAtou(maxExclusive, minInclusive))
    else SequenceCombinator(HYPHEN, netdevLinkSafeAtou(maxExclusive, minInclusive))
  ),
)

/**
 * Whether parse_range() accepts the already-extracted [word] with both bounds in
 * `[minInclusive, maxExclusive)` — the same check as [parseRange], done on a string rather than as a
 * grammar, for words whose quotes or escapes have to be removed first (see [ExtractedWordTerminal]).
 */
fun parseRangeAccepts(word: String, minInclusive: Long, maxExclusive: Long): Boolean {
  val dash = word.indexOf('-')
  val lower = if (dash < 0) word else word.substring(0, dash)
  if (lower.isEmpty()) return false
  if (!NetdevLinkSafeAtouTerminal.inRange(NetdevLinkSafeAtouTerminal.safeAtouValue(lower), minInclusive, maxExclusive)) return false
  if (dash < 0) return true
  val upper = word.substring(dash + 1)
  return upper.isNotEmpty() && NetdevLinkSafeAtouTerminal.inRange(NetdevLinkSafeAtouTerminal.safeAtouValue(upper), minInclusive, maxExclusive)
}

/**
 * [inner] optionally wrapped in a matching pair of `"…"` or `'…'`, the form a word takes after
 * extract_first_word(…, EXTRACT_UNQUOTE) has removed its quotes (systemd's own test-cpu-set-util.c
 * writes `8 '9' 10 "11"`). Only whole-word quoting is modelled here; pair it with an
 * [ExtractedWordTerminal] fallback (as [extractedRangeWord] does) for quotes that start mid-word.
 */
fun optionallyQuoted(inner: Combinator): Combinator = AlternativeCombinator(
  SequenceCombinator(LiteralChoiceTerminal("\""), inner, LiteralChoiceTerminal("\"")),
  SequenceCombinator(LiteralChoiceTerminal("'"), inner, LiteralChoiceTerminal("'")),
  inner,
)


// ---------------------------------------------------------------------------------------------------
// Words as extract_first_word() (src/basic/extract-word.c) returns them.

/**
 * A zero-width check that the input ends here or continues with one of [separators] (a regex character
 * class body such as `\s,`). Put after a structured word so that, under the classic first-match engine,
 * an [AlternativeCombinator] does not commit to a branch that matched only a prefix of the word.
 */
class WordEnd(separators: String) : Combinator {
  private val separator = Regex("[$separators]")

  private fun at(value: String, offset: Int): Boolean =
    offset == value.length || separator.matchesAt(value, offset)

  override fun SyntacticMatch(value: String, offset: Int): MatchResult =
    if (at(value, offset)) MatchResult(emptyList(), offset, emptyList(), offset) else NoMatch

  override fun SemanticMatch(value: String, offset: Int): MatchResult = SyntacticMatch(value, offset)

  override fun parse(value: String, offset: Int): Sequence<ParseStep> =
    if (at(value, offset)) sequenceOf(Parse(offset, emptyList())) else sequenceOf(Stuck(offset, setOf(this)))

  override fun toStringIndented(indent: Int): String = "WordEnd"
}

/**
 * One raw word of an extract_first_word(…, separators, EXTRACT_UNQUOTE[|EXTRACT_CUNESCAPE]) list, checked
 * the way systemd checks it: AFTER the quotes (which may open and close anywhere in the word, as in
 * `0-"3"`) and backslash escapes are removed.
 *
 * It matches a run of non-separator characters, backslash escapes and balanced quoted runs (quoted runs
 * may contain separators) — but ONLY when that word contains a quote or backslash, [extractWord] succeeds
 * on it and [check] accepts the result. Use it as the last alternative after a structured grammar for the
 * common spelling: the common spelling keeps its highlighting and error localization, and this only
 * rescues the exotic quoting and escaping systemd also accepts.
 *
 * It deliberately has no "syntactically right but semantically wrong" state. Under the classic engine
 * GrammarOptionValue locates a semantic error by lining the semantic match's longestMatch up against the
 * syntactic tokens; a whole-word token that only fails semantically would be misaligned with the
 * structured branch's partial progress and produce an out-of-range highlight. So an invalid word simply
 * does not match, and the value is reported as not matching the expected format.
 *
 * @param separators a regex character-class body for the separator set, e.g. `\s` or `\s,`
 * @param cunescape whether the caller passes EXTRACT_CUNESCAPE
 */
class ExtractedWordTerminal(
  separators: String,
  private val cunescape: Boolean,
  private val description: String,
  private val check: (String) -> Boolean,
) : TerminalCombinator {

  private val shape = Regex("""(?:[^$separators"'\\]|\\[\s\S]|"(?:[^"\\]|\\[\s\S])*"|'(?:[^'\\]|\\[\s\S])*')+""")

  /** The word at [offset] if it is one this terminal accepts, else null. */
  private fun word(value: String, offset: Int): String? {
    val text = shape.matchAt(value, offset)?.value ?: return null
    if (text.none { it == '"' || it == '\'' || it == '\\' }) return null
    return text.takeIf { extractWord(it, cunescape)?.let(check) == true }
  }

  override fun SyntacticMatch(value: String, offset: Int): MatchResult {
    val text = word(value, offset) ?: return NoMatch
    return MatchResult(listOf(text), offset + text.length, listOf(this), offset + text.length)
  }

  override fun SemanticMatch(value: String, offset: Int): MatchResult = SyntacticMatch(value, offset)

  override fun parse(value: String, offset: Int): Sequence<ParseStep> {
    val text = word(value, offset) ?: return sequenceOf(Stuck(offset, setOf(this)))
    return sequenceOf(Parse(offset + text.length, listOf(ParsedToken(offset, offset + text.length, text, this, true))))
  }

  override fun toString(): String = "ExtractedWord($description)"
}

/**
 * The text extract_first_word() returns for the single raw word [raw] under EXTRACT_UNQUOTE (plus
 * EXTRACT_CUNESCAPE when [cunescape]), or null where it fails: an unbalanced quote, a trailing
 * backslash, or — with [cunescape] — an escape cunescape_one() (src/basic/escape.c) refuses. Without
 * [cunescape], a backslash simply makes the next character literal.
 */
fun extractWord(raw: String, cunescape: Boolean): String? {
  val out = StringBuilder()
  var quote: Char? = null
  var i = 0
  while (i < raw.length) {
    val c = raw[i]
    if (c == '\\') {
      if (i + 1 >= raw.length) return null
      if (cunescape) {
        val (text, length) = cunescapeOne(raw, i + 1) ?: return null
        out.append(text)
        i += 1 + length
      } else {
        out.append(raw[i + 1])
        i += 2
      }
      continue
    }
    when {
      quote != null && c == quote -> quote = null
      quote == null && (c == '"' || c == '\'') -> quote = c
      else -> out.append(c)
    }
    i++
  }
  return if (quote == null) out.toString() else null
}

/** cunescape_one(…, accept_nul = false) on the escape starting at [p]: the decoded text and its length. */
private fun cunescapeOne(s: String, p: Int): Pair<String, Int>? {
  fun digits(count: Int, radix: Int): Int? =
    if (p + 1 + count > s.length) null else s.substring(p + 1, p + 1 + count).let { d -> if (d.all { Character.digit(it, radix) >= 0 }) d.toInt(radix) else null }

  return when (val c = s[p]) {
    'a' -> "\u0007" to 1
    'b' -> "\b" to 1
    'f' -> "\u000C" to 1
    'n' -> "\n" to 1
    'r' -> "\r" to 1
    't' -> "\t" to 1
    'v' -> "\u000B" to 1
    '\\', '"', '\'' -> c.toString() to 1
    's' -> " " to 1
    'x' -> digits(2, 16)?.takeIf { it != 0 }?.let { it.toChar().toString() to 3 }
    'u' -> digits(4, 16)?.takeIf { it != 0 && it !in 0xD800..0xDFFF }?.let { it.toChar().toString() to 5 }
    'U' -> {
      if (p + 9 > s.length) return null
      val v = s.substring(p + 1, p + 9).toLongOrNull(16) ?: return null
      if (v == 0L || v > 0x10FFFF || v in 0xD800..0xDFFF) return null
      String(Character.toChars(v.toInt())) to 9
    }
    in '0'..'7' -> {
      if (p + 3 > s.length) return null
      val d = s.substring(p, p + 3)
      if (!d.all { it in '0'..'7' }) return null
      val v = d.toInt(8)
      if (v == 0 || v > 255) null else v.toChar().toString() to 3
    }
    else -> null
  }
}

/**
 * One parse_range() word of a separator-split list: the structured `N[-M]` grammar (optionally quoted
 * as a whole) for highlighting, with an [ExtractedWordTerminal] fallback that accepts the same numbers
 * however extract_first_word's quoting and escaping spells them (`0-"3"`, `'0'-'3'`).
 */
fun extractedRangeWord(maxExclusive: Long, minInclusive: Long, separators: String, cunescape: Boolean): Combinator =
  AlternativeCombinator(
    SequenceCombinator(optionallyQuoted(parseRange(maxExclusive, minInclusive)), WordEnd(separators)),
    ExtractedWordTerminal(separators, cunescape, "range $minInclusive..${maxExclusive - 1}") {
      parseRangeAccepts(it, minInclusive, maxExclusive)
    },
  )


// ---------------------------------------------------------------------------------------------------
// CPU sets — config_parse_cpu_set (src/shared/cpu-set-util.c).

/** The largest CPU index cpu_set_add_range() accepts is CPU_SET_MAX_NCPU - 1 = 8191. */
private const val CPU_SET_MAX_NCPU = 8192L

// One extract_first_word() separator run: any mix of whitespace and commas (separators coalesce).
private val CPU_SET_SEPARATOR = RegexTerminal("""[\s,]+""", """[\s,]+""")

private val CPU_SET_ITEM = extractedRangeWord(CPU_SET_MAX_NCPU, 0L, """\s,""", cunescape = false)

/**
 * A CPU set: CPU indices and `N-M` ranges separated by whitespace and/or commas.
 *
 * config_parse_cpu_set splits with extract_first_word(…, WHITESPACE ",", EXTRACT_UNQUOTE), so separators
 * coalesce, leading and trailing separators are harmless (`0, 1, 2, 3, 4, 5, 6, 7, 63, ` is in
 * test-cpu-set-util.c), and a word may be quoted — anywhere in the word, not only around it (`0-"3"` is
 * `0-3`; see [extractedRangeWord]). Each word goes to parse_range(), whose bounds are
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
// Labeled so the number colours as a literal, like ByteSizeTerminal does.
private val SIZE_NUMBER = Labeled(Role.LITERAL, RegexTerminal("""\+?[0-9]+(?:\.[0-9]*)?""", """\+?[0-9]+(?:\.[0-9]*)?"""))

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
private val DURATION_NUMBER = Labeled(
  Role.LITERAL,
  RegexTerminal("""\+?[0-9]+(?:\.[0-9]+)?|\.[0-9]+""", """\+?[0-9]+(?:\.[0-9]+)?|\.[0-9]+"""),
)

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
 * compares whole components, so `/devices` is fine). Because path_simplify() runs first, `.` components
 * and repeated slashes in front of those names do not hide them (`/./dev/shm/key`, `//sys`). Spaces
 * are ordinary path characters. Lenient: path_is_valid()'s NAME_MAX/PATH_MAX length limits.
 */
val NON_API_VFS_ABSOLUTE_PATH = RegexTerminal(
  """[\s\S]+""",
  """(?!/(?:\.?/)*(?:dev|sys|proc)(?:/|$))$NO_DOT_DOT_COMPONENT/[\s\S]*""",
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
  SequenceCombinator(netdevLinkSafeAtou(NETDEV_LINK_UINT32_LIMIT, 1), EOF()),
  SequenceCombinator(ROUTE_TABLE_NAME, EOF()),
)


// ---------------------------------------------------------------------------------------------------
// Address prefixes — in_addr_prefix_from_string_auto_full (src/basic/in-addr-util.c).

/**
 * An IPv4 or IPv6 address with an optional `/prefixlen`, as in_addr_prefix_from_string_auto (mode
 * PREFIXLEN_FULL) reads it. Like [IP_ADDR_AND_ANY_PREFIX], but the length goes through
 * in_addr_parse_prefixlen → safe_atou8, so it is a base-0 number (`/0x18` is 24) up to the family width.
 * IPv6 is tried first so the IPv4 branch never strands part of an IPv6 literal.
 */
val IP_ADDR_AND_SAFE_PREFIX: Combinator = AlternativeCombinator(
  SequenceCombinator(IPV6_ADDR, ZeroOrOne(SequenceCombinator(CIDR_SEPARATOR, netdevLinkSafeAtou(129)))),
  SequenceCombinator(IPV4_ADDR, ZeroOrOne(SequenceCombinator(CIDR_SEPARATOR, netdevLinkSafeAtou(33)))),
)
