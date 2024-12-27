package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import kotlin.math.max

/**
 * This is a sequence of tokens that must match all of them.
 */
open class SequenceCombinator(vararg val tokens: Combinator) : Combinator {

  override fun SyntacticMatch(value: String, offset: Int): MatchResult {
    var index = offset
    val resultTokens = mutableListOf<String>()
    val resultTerminals = mutableListOf<TerminalCombinator>()
    var maxLength = 0

    for (token in tokens) {
      val match = token.SyntacticMatch(value, index)

      resultTokens.addAll(match.tokens)
      resultTerminals.addAll(match.terminals)
      maxLength = max(maxLength, match.longestMatch)
      if (match.matchResult == -1) {
        // No forward progress
        return MatchResult(resultTokens, -1, resultTerminals, maxLength)
      }

      index = match.matchResult


    }
    return MatchResult(resultTokens, index, resultTerminals, maxLength)
  }

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    var index = offset

    val resultTokens = mutableListOf<String>()
    val resultTerminals = mutableListOf<TerminalCombinator>()
    var maxLength = 0

    for (token in tokens) {
      val match = token.SemanticMatch(value, index)

      resultTokens.addAll(match.tokens)
      resultTerminals.addAll(match.terminals)
      maxLength = max(maxLength, match.longestMatch)

      if (match.matchResult == -1) {
        // No forward progress
        return MatchResult(resultTokens, -1, resultTerminals, maxLength)
      }
      index = match.matchResult

    }
    return MatchResult(resultTokens, index, resultTerminals, maxLength)
  }
}
