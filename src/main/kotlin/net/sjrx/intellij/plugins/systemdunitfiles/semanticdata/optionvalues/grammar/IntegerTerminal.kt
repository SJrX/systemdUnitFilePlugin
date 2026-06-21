package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

class IntegerTerminal(private val minInclusive: Long,private val maxExclusive: Long) : TerminalCombinator {

  constructor(minInclusive: Int, maxExclusive: Int) : this(minInclusive.toLong(), maxExclusive.toLong())

  val intRegex = "-?[0-9]+".toRegex()

  override fun SyntacticMatch(value: String, offset: Int): MatchResult {
    val matchResult = intRegex.matchAt(value, offset) ?: return NoMatch

    return MatchResult(listOf(matchResult.value), offset + matchResult.value.length, listOf(this), offset + matchResult.value.length)

  }

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    val matchResult = intRegex.matchAt(value, offset) ?: return NoMatch

    try {
      val intValue = matchResult.value.toLong()

      if (intValue < minInclusive || intValue >= maxExclusive) {
        return NoMatch.copy(longestMatch = offset)
      }

      return MatchResult(listOf(matchResult.value), offset + matchResult.value.length, listOf(this), offset + matchResult.value.length)
    } catch (e: NumberFormatException) {
      return NoMatch.copy(longestMatch = offset)
    }
  }

  override fun parse(value: String, offset: Int): Sequence<ParseStep> {
    val m = intRegex.matchAt(value, offset) ?: return sequenceOf(Stuck(offset, setOf(this)))
    val text = m.value
    // Lenient: any integer matches (so we can locate it); valid only if it is within range.
    val valid = text.toLongOrNull()?.let { it >= minInclusive && it < maxExclusive } ?: false
    return sequenceOf(Parse(offset + text.length, listOf(ParsedToken(offset, offset + text.length, text, this, valid))))
  }

  override fun toString(): String {
    return "Int($minInclusive,$maxExclusive)"
  }
}
