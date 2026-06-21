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

  override fun parse(value: String, offset: Int, frontier: Frontier): Sequence<Parse> {
    // Same as ZeroOrMore, but the first repetition is mandatory (and must make progress).
    fun extend(from: Parse): Sequence<Parse> = sequence {
      yield(from)
      for (step in combinator.parse(value, from.end, frontier)) {
        if (step.end > from.end) yieldAll(extend(Parse(step.end, from.tokens + step.tokens)))
      }
    }
    return combinator.parse(value, offset, frontier).filter { it.end > offset }.flatMap { extend(it) }
  }

  override fun toString(): String = toStringIndented(0)

  override fun toStringIndented(indent: Int): String {
    val prefix = "  ".repeat(indent)
    val sb = StringBuilder()
    sb.append(prefix).append("OneOrMore(\n")
    if (combinator is SequenceCombinator || combinator is AlternativeCombinator || combinator is Repeat || combinator is ZeroOrOne || combinator is ZeroOrMore || combinator is OneOrMore) {
      sb.append(combinator.toStringIndented(indent + 1)).append("\n")
    } else {
      sb.append("  ".repeat(indent + 1)).append(combinator.toString()).append("\n")
    }
    sb.append(prefix).append(")")
    return sb.toString()
  }
}
