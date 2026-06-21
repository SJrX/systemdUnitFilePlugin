package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import kotlin.math.max

/**
 * This is a sequence of tokens that must match any of them.
 */
open class AlternativeCombinator(vararg val tokens: Combinator) : Combinator {

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

  override fun parse(value: String, offset: Int): Sequence<ParseStep> =
    // Offer every alternative's steps (matches and dead ends), so option order no longer affects
    // correctness, and a failing branch still contributes what it expected.
    tokens.asSequence().flatMap { it.parse(value, offset) }

  override fun toString(): String = toStringIndented(0)

  override fun toStringIndented(indent: Int): String {
    val prefix = "  ".repeat(indent)
    val sb = StringBuilder()
    sb.append(prefix).append("Alt(\n")
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
