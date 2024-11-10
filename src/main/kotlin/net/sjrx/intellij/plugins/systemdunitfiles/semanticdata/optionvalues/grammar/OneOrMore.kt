package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import kotlin.math.max

/**
 * One Or More Combinator
 */
class OneOrMore(val combinator : Combinator) : Combinator {

  private fun match(value: String, offset: Int, f: (String, Int) -> MatchResult): MatchResult {
    var index = offset
    var match = f(value, index)

    val tokens = mutableListOf<String>()
    val terminals = mutableListOf<TerminalCombinator>()

    if (match.matchResult == -1) {
      return match
    }

    var maxLength = 0
    while (match.matchResult != -1) {
      index = match.matchResult
      tokens.addAll(match.tokens)
      terminals.addAll(match.terminals)

      match = f(value, index)

      maxLength = max(maxLength, match.longestMatch)
    }

    return MatchResult(tokens, index, terminals, maxLength)
  }

  override fun SyntacticMatch(value: String, offset: Int): MatchResult {
   return match(value, offset, combinator::SyntacticMatch)
  }

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    return match(value, offset, combinator::SemanticMatch)
  }
}
