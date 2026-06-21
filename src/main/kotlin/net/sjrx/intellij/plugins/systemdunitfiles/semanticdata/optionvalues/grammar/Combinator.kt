package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

interface Combinator {
  /**
   * WARNING: At the current time this combinator implementation doesn't necessarily guarantee a match.
   *
   * Seq(ZeroOrMore(Literal("fizz")), Literal("fizz"))
   *
   * If you try and match "fizz", the ZeroOrMore would greedily consume the fizz, and the second wouldn't match.
   *
   * I'm unclear if this will actually be a problem, and whether it's worth fixing.
   */

  /**
   * This checks the value string, starting at offset for a syntactic match.
   *
   * In a nutshell a syntactic match might accept things that we should color and try and analyze
   * but might be incorrect.
   *
   * For example if you something accepts a positive number, a syntactic regex should match any number even negative or floats
   *
   * The return value is -1 for no match, or a new offset if this token matched something.
   */
  fun SyntacticMatch(value : String, offset: Int): MatchResult

  /**
   * This checks the value string, starting at offset for a semantic match.
   *
   * In a nutshell a semantic match means we understood and it valid as far as the grammar is concerned.
   *
   * The return value is -1 for no match, or a new offset if this token matched something.
   */
  fun SemanticMatch(value : String, offset: Int): MatchResult

  /**
   * List-of-successes matcher (#467). Returns EVERY way this combinator can consume [value]
   * starting at [offset], lazily; an empty sequence means no match.
   *
   * This lives alongside Syntactic/SemanticMatch and is a single lenient pass: each [ParsedToken]
   * carries a `valid` flag for the strict (semantic) check. Because every alternative is offered
   * rather than the first greedy one committed to, matching is complete — e.g.
   * Seq(ZeroOrMore("a"), "a") on "aa" matches, because ZeroOrMore offers the shorter match too.
   */
  fun parse(value: String, offset: Int): Sequence<Parse>

  fun toStringIndented(indent: Int): String
}
