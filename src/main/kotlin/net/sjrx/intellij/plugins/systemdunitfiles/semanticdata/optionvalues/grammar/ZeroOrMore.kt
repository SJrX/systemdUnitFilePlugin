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

  override fun toString(): String = toStringIndented(0)

  override fun toStringIndented(indent: Int): String {
    val prefix = "  ".repeat(indent)
    val sb = StringBuilder()
    sb.append(prefix).append("ZeroOrMore(\n")
    if (combinator is SequenceCombinator || combinator is AlternativeCombinator || combinator is Repeat || combinator is ZeroOrOne || combinator is ZeroOrMore || combinator is OneOrMore) {
      sb.append(combinator.toStringIndented(indent + 1)).append("\n")
    } else {
      sb.append("  ".repeat(indent + 1)).append(combinator.toString()).append("\n")
    }
    sb.append(prefix).append(")")
    return sb.toString()
  }
}
