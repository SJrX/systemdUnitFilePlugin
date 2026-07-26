package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/**
 * An unsigned integer written the way systemd's `safe_atou*` family reads one, range-checked exactly.
 *
 * Those helpers are thin wrappers that pass base `0` to `strtoul()`:
 *
 * ```c
 * static inline int safe_atou8(const char *s, uint8_t *ret) {
 *         return safe_atou8_full(s, 0, ret);
 * }
 * ```
 *
 * (src/basic/parse-util.h; the same shape holds for safe_atou, safe_atou16 and safe_atou32.) Base 0
 * means C literal syntax, so `255`, `0xFF` and `0377` are all the same number and all valid input to
 * any setting parsed this way — systemd's own test data writes `TypeOfService=0x08`.
 *
 * [IntegerTerminal] only understands decimal, and an alternation of decimal / hex / octal branches
 * can only bound the decimal one, which silently drops the range check for the other two. This
 * terminal parses in the actual base instead, so `[0, maxExclusive)` is enforced however the number
 * is spelled.
 *
 * Note this is deliberately *not* used for values systemd reads with `parse_size()` (see
 * [ByteSizeTerminal]) or with a plain `strtoul(..., 10)`.
 *
 * @param minInclusive lowest accepted value
 * @param maxExclusive one past the highest accepted value
 * @see <a href="https://github.com/systemd/systemd/blob/a8e93919c3/src/basic/parse-util.h">parse-util.h</a>
 */
class UnsignedNumberTerminal(
  private val minInclusive: Long,
  private val maxExclusive: Long,
) : TerminalCombinator {

  private fun span(value: String, offset: Int): String? = SHAPE.matchAt(value, offset)?.value

  /** The numeric value of [text], or null when it doesn't fit a Long (and so is out of range anyway). */
  private fun valueOf(text: String): Long? = when {
    text.length > 2 && (text.startsWith("0x") || text.startsWith("0X")) -> text.drop(2).toLongOrNull(16)
    text.length > 1 && text.startsWith("0") -> text.drop(1).toLongOrNull(8)
    else -> text.toLongOrNull()
  }

  private fun inRange(text: String): Boolean = valueOf(text)?.let { it >= minInclusive && it < maxExclusive } ?: false

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
    // Lenient: any number-shaped run matches so it can be located and highlighted; valid only in range.
    return sequenceOf(Parse(offset + text.length, listOf(ParsedToken(offset, offset + text.length, text, this, inRange(text)))))
  }

  override fun toString(): String = "UInt($minInclusive,$maxExclusive)"

  private companion object {
    // Hex before octal before decimal, so "0x10" isn't read as the single digit 0 and "0377" isn't
    // read as three hundred and seventy-seven.
    val SHAPE = Regex("""0[xX][0-9a-fA-F]+|0[0-7]+|[0-9]+""")
  }
}
