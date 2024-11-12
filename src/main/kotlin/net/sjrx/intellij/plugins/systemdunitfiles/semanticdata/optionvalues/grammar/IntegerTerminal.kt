package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

class IntegerTerminal(private val minInclusive: Int,private val maxExclusive: Int) : TerminalCombinator {

  val intRegex = "-?[0-9]+".toRegex()

  override fun SyntacticMatch(value: String, offset: Int): MatchResult {
    val matchResult = intRegex.matchAt(value, offset) ?: return NoMatch

    return MatchResult(listOf(matchResult.value), offset + matchResult.value.length, listOf(this), offset + matchResult.value.length)

  }

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    val matchResult = intRegex.matchAt(value, offset) ?: return NoMatch

    val intValue = matchResult.value.toInt()

    if (intValue < minInclusive || intValue >= maxExclusive) {
      return NoMatch.copy(longestMatch = offset)
    }

    return MatchResult(listOf(matchResult.value), offset + matchResult.value.length, listOf(this), offset + matchResult.value.length)
  }
}
