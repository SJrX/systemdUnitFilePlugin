package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import java.math.BigDecimal
import java.math.BigInteger

/**
 * A byte size as read by systemd's `parse_size()`, range-checked on the value it denotes rather than
 * on how it is spelled.
 *
 * `parse_size(s, base)` accepts a run of `number[suffix]` elements, where the suffix selects a power
 * of `base` from the table for that base (`B`/`K`/`M`/`G`/`T`/`P`/`E` for base 1024) and a bare
 * number means bytes. A fractional part is allowed on all but the last element. Every setting here
 * uses base 1024.
 *
 * Bounding the spelling instead of the value is not good enough: `IPv6MTUBytes=1K` is 1024, which is
 * below IPV6_MIN_MTU, and no digit-count or character-class check can see that. This terminal
 * evaluates the sum and compares it, so the family minimum applies to every spelling.
 *
 * Only the single-element form is matched, which is what a size setting is written as in practice;
 * the multi-element form `parse_size()` also accepts (`1G 512M`) is not recognised, so it is reported
 * as malformed rather than silently mis-valued.
 *
 * @param minInclusive lowest accepted value in bytes
 * @param maxInclusive highest accepted value in bytes
 * @see <a href="https://github.com/systemd/systemd/blob/a8e93919c3/src/basic/parse-util.c">parse-util.c, parse_size()</a>
 */
class ByteSizeTerminal(
  private val minInclusive: BigInteger,
  private val maxInclusive: BigInteger,
) : TerminalCombinator {

  constructor(minInclusive: Long, maxInclusive: Long) :
    this(BigInteger.valueOf(minInclusive), BigInteger.valueOf(maxInclusive))

  private fun span(value: String, offset: Int): String? = SHAPE.matchAt(value, offset)?.value

  private fun inRange(text: String): Boolean {
    val match = SHAPE.matchEntire(text) ?: return false
    val digits = match.groupValues[1]
    val suffix = match.groupValues[2]
    val multiplier = MULTIPLIERS[suffix] ?: return false
    // parse_size() truncates towards zero, so "1.5K" is 1536 and "0.5B" is 0.
    val bytes = BigDecimal(digits).multiply(BigDecimal(multiplier)).toBigInteger()
    return bytes >= minInclusive && bytes <= maxInclusive
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

  override fun toString(): String = "Size($minInclusive,$maxInclusive)"

  private companion object {
    val SHAPE = Regex("""([0-9]+(?:\.[0-9]+)?)([EPTGMKB]?)""")

    /** table_1024 in parse_size(); the empty suffix is a plain byte count. */
    val MULTIPLIERS: Map<String, BigInteger> = buildMap {
      var scale = BigInteger.ONE
      for (suffix in listOf("B", "K", "M", "G", "T", "P", "E")) {
        put(suffix, scale)
        put("", BigInteger.ONE)
        scale = scale.multiply(BigInteger.valueOf(1024))
      }
    }
  }
}
