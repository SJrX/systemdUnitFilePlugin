package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/**
 * A workaround for the LEGACY (classic SyntacticMatch/SemanticMatch) engine only. Under the new
 * list-of-successes engine ([parse]/validate) it behaves exactly like [AlternativeCombinator].
 *
 * The classic engine's [AlternativeCombinator] returns the first branch that *syntactically* matches
 * and never backtracks. A [FlexibleLiteralChoiceTerminal] matches syntactically with a lenient regex
 * built from its choices' characters and capped at the longest choice, so a first branch can grab
 * part of a value that belongs to a later branch, and the enclosing sequence then fails on the
 * stranded rest. Every one of these is valid, and every one fails with a plain AlternativeCombinator:
 *
 *  - `Alt(Flex("yes", "no"), Flex("maybe"))` on `maybe`: the first regex `[a-z]{1,3}` takes `may`.
 *  - `Alt(Flex("ab"), Flex("abc"))` on `abc`: the exact prefix `ab` wins before `abc` is tried.
 *  - `Alt(Flex("none", "both", "ipv4", "ipv6"), BOOLEAN)` on `false`: the 4-char cap takes `fals`.
 *  - `Alt(Flex("connected", "disconnected"), BOOLEAN)` on `tRUE`: the lower-case class stops at `R`.
 *
 * This class first asks each branch for an exact (semantic) match and returns the first that has
 * one; only if none does it fall back to the usual lenient first-match, so an invalid value can
 * still be located and offered quick-fixes. Order still matters for that exact pass: put longer or
 * more specific branches first (e.g. names before [BOOLEAN], so `no` cannot be taken out of `none`).
 *
 * Can be replaced by a plain [AlternativeCombinator] once the legacy engine is removed.
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
