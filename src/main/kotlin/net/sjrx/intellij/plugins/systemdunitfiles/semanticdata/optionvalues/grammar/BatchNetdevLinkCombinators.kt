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
// parse_range() — src/basic/parse-util.c. Its bounds are safe_atou() numbers, modelled by [SafeAtoTerminal].

/**
 * Whether parse_range() (https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L305-L340)
 * accepts the already-extracted [word]: the text before the first `-` must satisfy [lower] and, if there is a `-`,
 * the non-empty rest must satisfy [upper]. A predicate for an [ExtractedWordTerminal], whose words have had their
 * quotes and escapes removed first; the structured grammar spells the same check out with the terminals.
 */
fun netdevLinkParseRangeAccepts(word: String, lower: SafeAtoTerminal, upper: SafeAtoTerminal): Boolean {
  val dash = word.indexOf('-')
  if (dash < 0) return lower.valueOf(word) != null
  val upperText = word.substring(dash + 1)
  return lower.valueOf(word.substring(0, dash)) != null && upperText.isNotEmpty() && upper.valueOf(upperText) != null
}


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

// ---------------------------------------------------------------------------------------------------
// CPU sets — config_parse_cpu_set (src/shared/cpu-set-util.c).

// One extract_first_word() separator run: any mix of whitespace and commas (separators coalesce).
private val CPU_SET_SEPARATOR = RegexTerminal("""[\s,]+""", """[\s,]+""")

// parse_range() bounds as safe_atou() reads them, below CPU_SET_MAX_NCPU (8192, cpu_set_add_range). The word was
// split on whitespace and commas, so no whitespace can reach safe_atou(). The lower bound is cut at the first `-`
// by parse_range(), so it never sees a minus; the upper bound is the rest of the word, which may (`0--0`).
private val CPU_SET_LOWER = SafeAtoTerminal(
  signed = false,
  base = 0,
  flags = emptySet(),
  minInclusive = 0L,
  maxInclusive = 8191L,
  acceptsWhitespace = false,
  acceptsMinus = false,
)
private val CPU_SET_UPPER = SafeAtoTerminal(
  signed = false,
  base = 0,
  flags = emptySet(),
  minInclusive = 0L,
  maxInclusive = 8191L,
  acceptsWhitespace = false,
  acceptsMinus = true,
)
private val CPU_SET_RANGE = SequenceCombinator(CPU_SET_LOWER, ZeroOrOne(SequenceCombinator(HYPHEN, CPU_SET_UPPER)))

// The same bounds for a word whose quotes have been removed: quoted whitespace does reach safe_atou() (`" 3"`).
private val CPU_SET_UNQUOTED_LOWER = SafeAtoTerminal(
  signed = false,
  base = 0,
  flags = emptySet(),
  minInclusive = 0L,
  maxInclusive = 8191L,
  acceptsWhitespace = true,
  acceptsMinus = false,
)
private val CPU_SET_UNQUOTED_UPPER = SafeAtoTerminal(
  signed = false,
  base = 0,
  flags = emptySet(),
  minInclusive = 0L,
  maxInclusive = 8191L,
  acceptsWhitespace = true,
  acceptsMinus = true,
)

// One word: the structured `N[-M]` grammar, bare or quoted as a whole, for highlighting; then an
// [ExtractedWordTerminal] fallback for quotes and escapes anywhere in the word (`0-"3"`, `'0'-'3'`).
private val CPU_SET_ITEM = AlternativeCombinator(
  SequenceCombinator(
    AlternativeCombinator(
      SequenceCombinator(LiteralChoiceTerminal("\""), CPU_SET_RANGE, LiteralChoiceTerminal("\"")),
      SequenceCombinator(LiteralChoiceTerminal("'"), CPU_SET_RANGE, LiteralChoiceTerminal("'")),
      CPU_SET_RANGE,
    ),
    WordEnd("""\s,"""),
  ),
  ExtractedWordTerminal("""\s,""", cunescape = false, "range 0..8191") { word ->
    netdevLinkParseRangeAccepts(word, CPU_SET_UNQUOTED_LOWER, CPU_SET_UNQUOTED_UPPER)
  },
)

/**
 * A CPU set: CPU indices and `N-M` ranges separated by whitespace and/or commas.
 *
 * config_parse_cpu_set splits with extract_first_word(…, WHITESPACE ",", EXTRACT_UNQUOTE), so separators
 * coalesce, leading and trailing separators are harmless (`0, 1, 2, 3, 4, 5, 6, 7, 63, ` is in
 * test-cpu-set-util.c), and a word may be quoted — anywhere in the word, not only around it (`0-"3"` is
 * `0-3`; the [ExtractedWordTerminal] fallback handles that). Each word goes to parse_range(), whose bounds are
 * safe_atou() numbers ([SafeAtoTerminal]: hex, octal, `0b`/`0o`) — and cpu_set_add_range() refuses anything at or above
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
// Address prefixes — in_addr_prefix_from_string_auto_full (src/basic/in-addr-util.c).

/**
 * An IPv4 or IPv6 address with an optional `/prefixlen`, as in_addr_prefix_from_string_auto (mode
 * PREFIXLEN_FULL) reads it. Like [IP_ADDR_AND_ANY_PREFIX], but the length goes through
 * in_addr_parse_prefixlen → safe_atou8, so it is a base-0 number (`/0x18` is 24, `/0b+1000` is 8) up to the
 * family width. Its only user, AllowedIPs=, splits the list on whitespace first, so no whitespace reaches
 * safe_atou8 (`/0b 1` is the two words `…/0b` and `1`). IPv6 is tried first so the IPv4 branch never strands
 * part of an IPv6 literal.
 */
val IP_ADDR_AND_SAFE_PREFIX: Combinator = AlternativeCombinator(
  SequenceCombinator(
    IPV6_ADDR,
    ZeroOrOne(
      SequenceCombinator(
        CIDR_SEPARATOR,
        SafeAtoTerminal(
          signed = false,
          base = 0,
          flags = emptySet(),
          minInclusive = 0L,
          maxInclusive = 128L,
          acceptsWhitespace = false,
          acceptsMinus = true,
        ),
      ),
    ),
  ),
  SequenceCombinator(
    IPV4_ADDR,
    ZeroOrOne(
      SequenceCombinator(
        CIDR_SEPARATOR,
        SafeAtoTerminal(
          signed = false,
          base = 0,
          flags = emptySet(),
          minInclusive = 0L,
          maxInclusive = 32L,
          acceptsWhitespace = false,
          acceptsMinus = true,
        ),
      ),
    ),
  ),
)


// ---------------------------------------------------------------------------------------------------
// Checks on a whole matched span — for rules systemd applies to the parsed value rather than its shape.

/**
 * Matches exactly what [inner] matches, but the match is only semantically valid when [check] accepts
 * the matched text. For limits systemd applies after parsing, which a grammar cannot spell out: a
 * magnitude bound on a parse_size() total, or "must (not) be a multicast address".
 *
 * Under the parse engine a rejected span marks its first token invalid; under the classic engine the
 * semantic match fails with no progress at the span's start. Either way the first token of the span is
 * highlighted, and the value keeps the structured grammar's colouring.
 */
class NetdevLinkCheckedSpan(
  private val inner: Combinator,
  private val description: String,
  private val check: (String) -> Boolean,
) : Combinator {

  override fun SyntacticMatch(value: String, offset: Int): MatchResult = inner.SyntacticMatch(value, offset)

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    val match = inner.SemanticMatch(value, offset)
    if (match.matchResult == -1 || check(value.substring(offset, match.matchResult))) return match
    return NoMatch.copy(longestMatch = offset)
  }

  override fun parse(value: String, offset: Int): Sequence<ParseStep> =
    inner.parse(value, offset).map { step ->
      if (step is Parse && step.tokens.isNotEmpty() && !check(value.substring(offset, step.end))) {
        step.copy(tokens = listOf(step.tokens.first().copy(valid = false)) + step.tokens.drop(1))
      } else {
        step
      }
    }

  override fun toString(): String = "Checked($description)"

  override fun toStringIndented(indent: Int): String = "  ".repeat(indent) + "Checked($description,\n" + inner.toStringIndented(indent + 1) + ")"
}

/**
 * in_addr_is_multicast() (src/basic/in-addr-util.c) on an address literal [IP_ADDR] has matched: IPv4
 * in 224.0.0.0/4 (IN_MULTICAST), IPv6 in ff00::/8 (IN6_IS_ADDR_MULTICAST). The first IPv6 byte is 0xff
 * only when the first group is written with four hex digits starting `ff` — `ff::1` is 0x00ff::1, and an
 * address starting `::` (including IPv4-mapped `::ffff:239.1.1.1`) has a zero first byte.
 */
fun netdevLinkIsMulticast(address: String): Boolean {
  if (':' in address) {
    val first = address.substringBefore(':')
    return first.length == 4 && first.startsWith("ff", ignoreCase = true)
  }
  return address.substringBefore('.').toIntOrNull() in 224..239
}

/**
 * The total parse_size(text, 1000, …) (src/basic/parse-util.c) computes for a value [siSize] has
 * matched: each element contributes `l * factor + (unsigned long long) (frac * factor)`. Exact decimal
 * arithmetic stands in for the C double, which only matters within a unit of the truncated fraction.
 * Null if [text] does not have [siSize]'s shape.
 */
fun netdevLinkParseSizeSi(text: String): BigInteger? {
  val factors = mapOf("E" to 18, "P" to 15, "T" to 12, "G" to 9, "M" to 6, "K" to 3, "B" to 0, "" to 0)
  val element = Regex("""\s*\+?([0-9]+)(?:\.([0-9]*))?\s*([EPTGMKB]?)""")
  var total = BigInteger.ZERO
  var pos = 0
  while (pos < text.length) {
    val m = element.matchAt(text, pos) ?: return null
    if (m.value.isEmpty()) return null
    val factor = BigInteger.TEN.pow(factors.getValue(m.groupValues[3]))
    val fraction = m.groupValues[2].takeIf { it.isNotEmpty() }
      ?.let { java.math.BigDecimal("0.$it").multiply(java.math.BigDecimal(factor)).toBigInteger() } ?: BigInteger.ZERO
    total += BigInteger(m.groupValues[1]) * factor + fraction
    pos = m.range.last + 1
  }
  return total
}
