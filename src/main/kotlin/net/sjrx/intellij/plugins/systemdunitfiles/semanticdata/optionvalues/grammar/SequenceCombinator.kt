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

  override fun parse(value: String, offset: Int): Sequence<Parse> {
    // Thread each possibility of one part into the next: the cartesian product of the parts.
    var results = sequenceOf(Parse(offset, emptyList()))
    for (token in tokens) {
      results = results.flatMap { acc ->
        token.parse(value, acc.end).map { next -> Parse(next.end, acc.tokens + next.tokens) }
      }
    }
    return results
  }

  override fun toString(): String = toStringIndented(0)

  override fun toStringIndented(indent: Int): String {
    val prefix = "  ".repeat(indent)
    val sb = StringBuilder()
    sb.append(prefix).append("Seq(\n")
    for (token in tokens) {
      if (token is SequenceCombinator || token is AlternativeCombinator || token is Repeat || token is ZeroOrOne || token is ZeroOrMore || token is OneOrMore) {
        sb.append(token.toStringIndented(indent + 1)).append("\n")
      } else {
        sb.append("  ".repeat(indent + 1)).append(token.toString()).append("\n")
      }
    }
    sb.append(prefix).append(")")
    return sb.toString()
  }
}
