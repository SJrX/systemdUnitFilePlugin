package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/**
 * What the InvalidValue inspection would report for a value, computed purely from a whole-value grammar, once
 * per grammar engine. Lets combinator tests assert verdicts and highlighted ranges for both engines in one run,
 * whatever `-Dsystemd.unit.grammarParseEngine` says.
 *
 * Each function mirrors the range logic of its engine's branch in [GrammarOptionValue.generateProblemDescriptors];
 * keep them in step with it.
 */
data class GrammarEngineVerdict(val kind: Kind, val start: Int, val end: Int) {

  enum class Kind { VALID, SYNTAX, SEMANTIC }

  override fun toString(): String = if (kind == Kind.VALID) "VALID" else "$kind[$start,$end)"

  companion object {
    val VALID = GrammarEngineVerdict(Kind.VALID, 0, 0)

    /** The classic SyntacticMatch / SemanticMatch engine. */
    fun classic(grammar: Combinator, value: String): GrammarEngineVerdict {
      val syntactic = grammar.SyntacticMatch(value, 0)
      if (syntactic.matchResult == -1) {
        return if (syntactic.longestMatch < value.length) {
          GrammarEngineVerdict(Kind.SYNTAX, syntactic.longestMatch, value.length)
        } else {
          GrammarEngineVerdict(Kind.SYNTAX, 0, value.length)
        }
      }

      val semantic = grammar.SemanticMatch(value, 0)
      if (semantic.matchResult == -1) {
        if (semantic.tokens.size < syntactic.tokens.size) {
          var tokenLength = 0
          var tokenIndex = 0
          for (token in syntactic.tokens) {
            tokenLength += token.length
            if (tokenLength > semantic.longestMatch) break
            tokenIndex++
          }
          val start = semantic.longestMatch
          return GrammarEngineVerdict(Kind.SEMANTIC, start, start + syntactic.tokens[tokenIndex].length)
        }
        return GrammarEngineVerdict(Kind.SEMANTIC, 0, value.length)
      }

      return VALID
    }

    /** The list-of-successes engine ([Combinator.validate]). */
    fun parseEngine(grammar: Combinator, value: String): GrammarEngineVerdict =
      when (val outcome = grammar.validate(value)) {
        is ParseOutcome.Valid -> VALID
        is ParseOutcome.SyntaxError -> if (outcome.furthest < value.length) {
          GrammarEngineVerdict(Kind.SYNTAX, outcome.furthest, value.length)
        } else {
          GrammarEngineVerdict(Kind.SYNTAX, 0, value.length)
        }
        is ParseOutcome.SemanticError -> GrammarEngineVerdict(Kind.SEMANTIC, outcome.badToken.start, outcome.badToken.end)
      }
  }
}
