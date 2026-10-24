package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/**
 * An [AlternativeCombinator] whose *syntactic* pass first looks for a branch that is already valid
 * at this offset, and only falls back to the usual first-syntactic-match when none is.
 *
 * Under the classic SyntacticMatch/SemanticMatch engine a plain AlternativeCombinator commits to the
 * first branch that *syntactically* matches. That goes wrong for "one of these names, or a boolean":
 * the names terminal's lenient shape regex happily matches part of a perfectly good boolean — the `t`
 * of `tRUE` (its regex is lower-case only), or the `fals` of `false` when no name is five characters
 * long — and the enclosing sequence then fails at the stranded remainder. Asking each branch for a
 * semantic match first lets the boolean win when it is the right reading, while an invalid value still
 * falls through to the lenient branch so it can be located and offered quick-fixes.
 *
 * The list-of-successes engine ([parse]) already tries every branch, so it is inherited unchanged.
 *
 * Order still matters for the semantic pass: put the names before the boolean, so a prefix such as
 * the `no` in `none` cannot be taken by the boolean.
 */
class PreferValidAlternativeCombinator(vararg tokens: Combinator) : AlternativeCombinator(*tokens) {

  override fun SyntacticMatch(value: String, offset: Int): MatchResult {
    for (token in tokens) {
      val match = token.SemanticMatch(value, offset)
      if (match.matchResult != -1) {
        return match
      }
    }
    return super.SyntacticMatch(value, offset)
  }
}
