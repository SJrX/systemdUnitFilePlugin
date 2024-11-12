package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import kotlin.math.max

/**
 * Zero Or More Combinator
 */
class ZeroOrMore(val combinator : Combinator) : Combinator {

  private fun match(value: String, offset: Int, f: (String, Int) -> MatchResult): MatchResult {
    var index = offset
    val tokens = mutableListOf<String>()
    val terminals = mutableListOf<TerminalCombinator>()

    var match = f(value, index)


    if (match.matchResult == -1) {
      return MatchResult(tokens, offset, terminals, match.longestMatch)
    }

    var maxLength = match.longestMatch


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
