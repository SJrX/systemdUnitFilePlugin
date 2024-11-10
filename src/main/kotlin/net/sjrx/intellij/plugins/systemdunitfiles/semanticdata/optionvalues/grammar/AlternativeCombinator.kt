package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import kotlin.math.max

/**
 * This is a sequence of tokens that must match any of them.
 */
class AlternativeCombinator(vararg val tokens: Combinator) : Combinator {

  fun match(value: String, offset: Int, f: (Combinator, String, Int) -> MatchResult): MatchResult {


    var longestTokenMatch = emptyList<String>()
    var longestTerminalMatch = emptyList<TerminalCombinator>()
    var maxLength = 0

    for (token in tokens) {
      val match = f(token, value, offset)
      if (match.matchResult != -1) {
        return match
      }



      if (match.tokens.size > longestTerminalMatch.size) {
        longestTerminalMatch = match.terminals
        longestTokenMatch = match.tokens
        maxLength = max(maxLength, match.longestMatch)
      }
    }

    return MatchResult(longestTokenMatch, -1, longestTerminalMatch, maxLength)
  }

  override fun SyntacticMatch(value: String, offset: Int): MatchResult {
    return match(value, offset, Combinator::SyntacticMatch)
  }

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    return match(value, offset, Combinator::SemanticMatch)
  }
}
