package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import java.math.BigInteger

/**
 * An integer exactly as systemd's `safe_ato*()` family reads it. That is safe_atoi and safe_atolli (and so
 * safe_atoi32, safe_atoi64 and safe_atoli), safe_atou_full and safe_atollu_full (and so safe_atou, safe_atou8,
 * safe_atou16, safe_atou32, safe_atou64, safe_atolu, safe_atozu, safe_atoux16 and safe_atoux64), including an
 * explicit base and the `SAFE_ATO_REFUSE_*` flags. Everything here is pinned to systemd 0f0682b773 (the plugin's
 * `last_commit_hash`) and was confirmed against the real parsers with steve-local/systemd-tools/bin/sd-check.
 *
 * ## What systemd does
 *
 * Source: [mangle_base](https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L403-L427),
 * [safe_atou_full](https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478),
 * [safe_atoi](https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L497-L520),
 * [safe_atollu_full](https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L522-L558),
 * [safe_atolli](https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L560-L581),
 * [safe_atou8_full / safe_atou16_full / safe_atoi16](https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L583-L638)
 * and the [wrappers](https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.h#L27-L122).
 *
 * 1. With [Flag.REFUSE_LEADING_WHITESPACE], a first character in WHITESPACE (`" \t\n\r"`) is -EINVAL.
 * 2. Skip leading WHITESPACE (strspn). Note that `\v` and `\f` are not in it.
 * 3. With [Flag.REFUSE_PLUS_MINUS], a `+` or `-` here is -EINVAL. With [Flag.REFUSE_LEADING_ZERO], a `0` here is
 *    -EINVAL unless the rest is exactly `0`.
 * 4. `mangle_base()`, in base 0 only: a `0b`/`0B` here switches to base 2 and `0o`/`0O` to base 8, and the prefix is
 *    cut off. The signed helpers always use base 0 and take no flags.
 * 5. strtoul/strtoull/strtol/strtoll on the rest. These skip C `isspace()` (which does include `\v` and `\f`) and
 *    take one optional `+` or `-`. Then, in base 0, they read `0x`/`0X` hex (only if a hex digit follows), a
 *    leading-`0` octal number, or a decimal one. In base 16 an optional `0x`/`0X` is skipped first. Bases 8 and 10
 *    take plain digits. systemd turns off glibc's C23 `0b` support on purpose
 *    ([src/include/glibc/stdlib.h](https://github.com/systemd/systemd/blob/0f0682b773/src/include/glibc/stdlib.h#L4-L15)),
 *    so `+0b1` and `-0b0` are read as `+0`/`-0` followed by junk.
 * 6. The whole string must be consumed (`*x != 0` is -EINVAL), so trailing whitespace is an error.
 * 7. Range checks (-ERANGE). First strto*l's own overflow (more than 64 bits). Then, for the unsigned helpers,
 *    `l != 0 && s[0] == '-'`, where `s` is the text after step 4, so `-0` is fine and `-1` is not. Then the width
 *    check of the specific helper (`(unsigned) l != l`, `u > UINT8_MAX`, `(int) l != l`, …).
 *
 * Some consequences, all confirmed with sd-check:
 *  - accepted: `0b101`, `0O17`, `+5`, `-0`, `-0x0`, `-00`, `0b+1`, `0b-0`, `0b 1`, `0o 7`. Whitespace and a sign may
 *    follow the `0b`/`0o` prefix, because strtoul starts afresh there. Also `0x7fffffff` for an int, and `0b-1` and
 *    `0o -10` for a signed int.
 *  - malformed (-EINVAL): `08`, `0x`, `0b`, `0b2`, `0o8`, `+0b1`, `-0b0`, `0b0b1`, `0x0x1`, `+-1`, `+`, `- 5`, `5 `.
 *  - out of range (-ERANGE): `-1` and `0b-1` for any unsigned helper. For a uint16, `65536`, `0200000` and
 *    `0x10000`. For a uint64, `18446744073709551616`.
 *  - the strtoul wrap-around. A `-` that is *not* at `s[0]` escapes the minus check, and strtoul negates modulo
 *    2^64. So for safe_atou64, `0b -1` and `\v-1` are 18446744073709551615 and accepted. For safe_atou32 they are out
 *    of range, but `0b -` followed by sixty-four `1`s is 1 and accepted. This terminal reproduces that. It assumes an
 *    LP64 target such as x86-64 or aarch64, where `unsigned long` is 64 bits.
 *  - the flags only look at the first character after step 2, so parse_mode()'s REFUSE_PLUS_MINUS still accepts
 *    `UMask=\v+22`, and parse_uid()'s REFUSE_LEADING_WHITESPACE still accepts a leading `\v`.
 *
 * ## Syntax versus range
 *
 * The token this terminal claims is exactly what strtoul() consumes, so a malformed number is a *syntax* error
 * located where strtoul stopped (`08` matches `0` and leaves `8`; `0b2` gets stuck at `2`). A well-formed number
 * outside `[minInclusive, maxInclusive]`, including the minus check, is a *semantic* error on the number token.
 * When leading whitespace is allowed it is reported as a separate whitespace token, so the highlighted bad token is
 * the number alone.
 *
 * ## Choosing the parameters
 *
 * Every parameter is required. Write them out with named arguments.
 *
 * @param signed true for safe_atoi, safe_atoi16 and safe_atolli (strtol: no wrap-around and no minus check);
 *   false for every safe_atou* helper.
 * @param base the base the helper passes: 0 for every helper without an explicit base (the usual case, with
 *   `0b`/`0o`/`0x`/leading-`0`), 16 for safe_atoux16, safe_atoux64 and `safe_atou*_full(…, 16, …)`, 8 for
 *   parse_mode(), 10 for parse_uid(). Signed helpers only exist with base 0.
 * @param flags the `SAFE_ATO_REFUSE_*` flags the helper passes, usually none. Signed helpers take no flags. The
 *   config-file callers that pass flags are
 *   [parse_mode](https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L90-L110) (base 8,
 *   REFUSE_PLUS_MINUS),
 *   [parse_ip_port](https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L718-L734) (base 0,
 *   REFUSE_LEADING_WHITESPACE, then 0 is refused) and
 *   [parse_uid](https://github.com/systemd/systemd/blob/0f0682b773/src/basic/user-util.c#L66-L95) (base 10, all
 *   three).
 * @param minInclusive smallest accepted value: the type minimum, or a caller's own bound (for example 1 when the
 *   caller refuses 0 afterwards).
 * @param maxInclusive largest accepted value: the helper's type maximum ([UINT8_MAX], [UINT16_MAX], [UINT32_MAX],
 *   [UINT64_MAX], [INT32_MAX], [INT64_MAX], …) or a tighter caller bound.
 * @param acceptsWhitespace true when whitespace can reach the parser. That is the case for a whole value (already
 *   stripped at both ends, but `0b 1` and a leading `\v` still matter), or for a field the caller cut out on a
 *   non-whitespace separator such as `:` or `/`. false inside a word that was split on whitespace, where `0b 1` is
 *   two words.
 * @param acceptsMinus true when a `-` can reach the parser. false only where the caller has already cut the text at
 *   the first `-`, for example the lower bound of parse_range(), so `-0` there is an empty word (-EINVAL).
 */
class SafeAtoTerminal(
  val signed: Boolean,
  val base: Int,
  val flags: Set<Flag>,
  val minInclusive: BigInteger,
  val maxInclusive: BigInteger,
  val acceptsWhitespace: Boolean,
  val acceptsMinus: Boolean,
) : TerminalCombinator {

  /** Same as the primary constructor, for bounds that fit a Long (everything except values above INT64_MAX). */
  constructor(
    signed: Boolean,
    base: Int,
    flags: Set<Flag>,
    minInclusive: Long,
    maxInclusive: Long,
    acceptsWhitespace: Boolean,
    acceptsMinus: Boolean,
  ) : this(signed, base, flags, BigInteger.valueOf(minInclusive), BigInteger.valueOf(maxInclusive), acceptsWhitespace, acceptsMinus)

  /** systemd's `SAFE_ATO_REFUSE_*` flags (src/basic/parse-util.h). */
  enum class Flag {
    /** `SAFE_ATO_REFUSE_PLUS_MINUS`: a leading `+` or `-` is -EINVAL. */
    REFUSE_PLUS_MINUS,

    /** `SAFE_ATO_REFUSE_LEADING_ZERO`: a leading `0` is -EINVAL unless the whole number is `0`. */
    REFUSE_LEADING_ZERO,

    /** `SAFE_ATO_REFUSE_LEADING_WHITESPACE`: a leading `" \t\n\r"` is -EINVAL. */
    REFUSE_LEADING_WHITESPACE,
  }

  init {
    require(base == 0 || base == 8 || base == 10 || base == 16) { "systemd's config parsers use base 0, 8, 10 or 16, not $base" }
    require(!signed || (base == 0 && flags.isEmpty())) { "systemd's signed safe_ato* helpers take no base and no flags" }
    require(minInclusive <= maxInclusive) { "empty range [$minInclusive, $maxInclusive]" }
    if (signed) {
      require(minInclusive >= INT64_MIN && maxInclusive <= INT64_MAX) { "a signed range must lie within int64" }
    } else {
      require(minInclusive >= BigInteger.ZERO && maxInclusive <= UINT64_MAX) { "an unsigned range must lie within uint64" }
    }
  }

  /** What one scan at an offset found: either a number spanning [numberStart, end) or a dead end at [stuckAt]. */
  private sealed interface Scan

  /**
   * strtoul consumed [numberStart, end); any whitespace before [numberStart] (from offset) was skipped. [value] is
   * what systemd stores, or null when a range check fails.
   */
  private data class Number(val numberStart: Int, val end: Int, val value: BigInteger?) : Scan {
    val valid: Boolean get() = value != null
  }

  /** No number here (-EINVAL); [stuckAt] is where one was expected. */
  private data class NoNumber(val stuckAt: Int) : Scan

  private fun scan(value: String, offset: Int): Scan {
    val n = value.length
    var p = offset

    if (acceptsWhitespace) {
      if (Flag.REFUSE_LEADING_WHITESPACE in flags && p < n && value[p] in WHITESPACE) return NoNumber(p)
      // s += strspn(s, WHITESPACE)
      while (p < n && value[p] in WHITESPACE) p++
    }

    if (Flag.REFUSE_PLUS_MINUS in flags && p < n && (value[p] == '+' || value[p] == '-')) return NoNumber(p)

    if (Flag.REFUSE_LEADING_ZERO in flags && p < n && value[p] == '0') {
      // Only a lone "0" survives the flag, so the number is that one digit and anything after it is junk.
      return Number(p, p + 1, checkedValue(BigInteger.ZERO, negative = false, minusAtS = false))
    }

    // mangle_base(): only in base 0, only at the very start of what is left.
    if (base == 0 && p + 1 < n && value[p] == '0' && value[p + 1] in "bBoO") {
      val radix = if (value[p + 1] == 'b' || value[p + 1] == 'B') 2 else 8
      return strtoul(value, p + 2, numberStart = p, radix = radix)
    }

    // strtoul() itself skips isspace() (which, unlike WHITESPACE, includes \v and \f); the token starts after it.
    var q = p
    if (acceptsWhitespace) {
      while (q < n && value[q] in ISSPACE) q++
    }
    return strtoul(value, p, numberStart = q, radix = base)
  }

  /**
   * strtoul()/strtol() on value[s..], with the number token starting at [numberStart] (after any whitespace already
   * skipped). [radix] 0 means C literal syntax; 2 and 8 also come from mangle_base().
   */
  private fun strtoul(value: String, s: Int, numberStart: Int, radix: Int): Scan {
    val n = value.length
    var q = s
    if (acceptsWhitespace) {
      while (q < n && value[q] in ISSPACE) q++
    }

    var negative = false
    val signAt = q
    if (q < n && value[q] == '+') {
      q++
    } else if (acceptsMinus && q < n && value[q] == '-') {
      negative = true
      q++
    }

    var digitRadix = radix
    if ((radix == 0 || radix == 16) &&
      q + 2 < n && value[q] == '0' && (value[q + 1] == 'x' || value[q + 1] == 'X') && value[q + 2].isDigitOf(16)
    ) {
      // "0x" only counts as a prefix when a hex digit follows; otherwise glibc reads the "0" and stops at 'x'.
      q += 2
      digitRadix = 16
    } else if (radix == 0) {
      digitRadix = if (q < n && value[q] == '0') 8 else 10
    }

    val digitsStart = q
    while (q < n && value[q].isDigitOf(digitRadix)) q++
    if (q == digitsStart) return NoNumber(digitsStart)

    val magnitude = BigInteger(value.substring(digitsStart, q), digitRadix)
    return Number(numberStart, q, checkedValue(magnitude, negative, minusAtS = negative && signAt == s))
  }

  /**
   * The -ERANGE checks: strto*l overflow, safe_atou*'s minus check (with strtoul's wrap-around), the width/bounds.
   * Returns the value systemd stores, or null when one of them fails.
   */
  private fun checkedValue(magnitude: BigInteger, negative: Boolean, minusAtS: Boolean): BigInteger? {
    val v = if (signed) {
      if (negative) magnitude.negate() else magnitude
    } else {
      if (magnitude > UINT64_MAX) return null // strtoul overflow
      if (!negative || magnitude.signum() == 0) {
        magnitude
      } else {
        if (minusAtS) return null // l != 0 && s[0] == '-'
        TWO_TO_THE_64.subtract(magnitude) // strtoul negates modulo 2^64
      }
    }
    // For signed values this also covers strtol's own overflow, since the bounds lie within int64.
    return if (v >= minInclusive && v <= maxInclusive) v else null
  }

  /**
   * The value systemd would store for the whole of [text], or null if this terminal would flag it (malformed,
   * out of range, or not consuming all of [text]). For predicates that need the number itself, such as a check
   * against a set of allowed values or a comparison between two fields.
   */
  fun valueOf(text: String): BigInteger? {
    val scan = scan(text, 0)
    return if (scan is Number && scan.end == text.length) scan.value else null
  }

  /** An ASCII digit in [radix] (the C locale; Unicode digits are not digits to strtoul). */
  private fun Char.isDigitOf(radix: Int): Boolean =
    (this in '0'..'9' && this - '0' < radix) || (radix == 16 && (this in 'a'..'f' || this in 'A'..'F'))

  override fun SyntacticMatch(value: String, offset: Int): MatchResult = when (val scan = scan(value, offset)) {
    is NoNumber -> NoMatch.copy(longestMatch = scan.stuckAt)
    is Number -> matched(value, offset, scan)
  }

  override fun SemanticMatch(value: String, offset: Int): MatchResult = when (val scan = scan(value, offset)) {
    is NoNumber -> NoMatch.copy(longestMatch = scan.stuckAt)
    // Point longestMatch at the number (past any leading whitespace) so the classic engine highlights it.
    is Number -> if (scan.valid) matched(value, offset, scan) else NoMatch.copy(longestMatch = scan.numberStart)
  }

  private fun matched(value: String, offset: Int, scan: Number): MatchResult {
    val number = value.substring(scan.numberStart, scan.end)
    return if (scan.numberStart == offset) {
      MatchResult(listOf(number), scan.end, listOf(this), scan.end)
    } else {
      MatchResult(listOf(value.substring(offset, scan.numberStart), number), scan.end, listOf(LEADING_WHITESPACE, this), scan.end)
    }
  }

  override fun parse(value: String, offset: Int): Sequence<ParseStep> = when (val scan = scan(value, offset)) {
    is NoNumber -> sequenceOf(Stuck(scan.stuckAt, setOf(this)))
    is Number -> {
      val number = ParsedToken(scan.numberStart, scan.end, value.substring(scan.numberStart, scan.end), this, scan.valid)
      val tokens = if (scan.numberStart == offset) {
        listOf(number)
      } else {
        listOf(ParsedToken(offset, scan.numberStart, value.substring(offset, scan.numberStart), LEADING_WHITESPACE, true), number)
      }
      sequenceOf(Parse(scan.end, tokens))
    }
  }

  override fun toString(): String =
    "SafeAto(${if (signed) "signed" else "unsigned"},base=$base${flags.joinToString("") { ",${it.name}" }}," +
      "[$minInclusive,$maxInclusive]${if (acceptsWhitespace) ",ws" else ""}${if (acceptsMinus) "" else ",no-minus"})"

  companion object {
    /** systemd's WHITESPACE, which safe_ato* skip with strspn() before mangle_base(). */
    private const val WHITESPACE = " \t\n\r"

    /** C isspace() in the C locale, which strto*l() skip before the sign. */
    private const val ISSPACE = " \t\n\u000B\u000C\r"

    /** The terminal recorded for skipped leading whitespace, so it is never coloured or highlighted as the number. */
    private val LEADING_WHITESPACE = WhitespaceTerminal()

    private val TWO_TO_THE_64: BigInteger = BigInteger.ONE.shiftLeft(64)

    val UINT8_MAX: BigInteger = BigInteger.valueOf(255)
    val UINT16_MAX: BigInteger = BigInteger.valueOf(65_535)
    val UINT32_MAX: BigInteger = BigInteger.valueOf(4_294_967_295L)
    val UINT64_MAX: BigInteger = TWO_TO_THE_64.subtract(BigInteger.ONE)
    val INT16_MIN: BigInteger = BigInteger.valueOf(Short.MIN_VALUE.toLong())
    val INT16_MAX: BigInteger = BigInteger.valueOf(Short.MAX_VALUE.toLong())
    val INT32_MIN: BigInteger = BigInteger.valueOf(Int.MIN_VALUE.toLong())
    val INT32_MAX: BigInteger = BigInteger.valueOf(Int.MAX_VALUE.toLong())
    val INT64_MIN: BigInteger = BigInteger.valueOf(Long.MIN_VALUE)
    val INT64_MAX: BigInteger = BigInteger.valueOf(Long.MAX_VALUE)
  }
}
