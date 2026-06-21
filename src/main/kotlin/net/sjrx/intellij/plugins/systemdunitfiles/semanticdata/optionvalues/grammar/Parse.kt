package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/*
 * List-of-successes matcher (GitHub #467, step 2).
 *
 * These types support a second matching method, `Combinator.parse()`, that lives ALONGSIDE the
 * existing SyntacticMatch / SemanticMatch on every combinator. Nothing here is wired into
 * GrammarOptionValue yet — the goal is to flesh the approach out on the real combinators and
 * validate it against the real grammars in tests before deciding to migrate the caller.
 *
 * Where the existing engine returns ONE greedy result and runs two near-identical passes, parse()
 * returns EVERY way a combinator can match (lazily), and folds the strict "semantic" check into a
 * `valid` flag on each token. So one lenient pass answers both questions, and greedy traps like
 * Seq(ZeroOrMore("a"), "a") on "aa" resolve themselves (see Combinator.parse docs).
 */

/** A single terminal token, with the strict-validity verdict (the old "semantic" check) folded in. */
data class ParsedToken(
  val start: Int,
  val end: Int,
  val text: String,
  val terminal: TerminalCombinator,
  val valid: Boolean,
)

/** One way a combinator consumed input from some offset: it ended at [end], producing [tokens]. */
data class Parse(val end: Int, val tokens: List<ParsedToken>)

/** The outcome of validating a whole value against a grammar via parse(). */
sealed interface ParseOutcome {
  /** Some path consumed the whole value with every token strictly valid. */
  object Valid : ParseOutcome

  /** A path consumed the whole value, but a token is not strictly valid (well-formed but wrong). */
  data class SemanticError(val badToken: ParsedToken) : ParseOutcome

  /** No path consumed the whole value. [furthest] is how far any path got (for error localization). */
  data class SyntaxError(val furthest: Int) : ParseOutcome
}

/** Every way [this] grammar can consume the entire [value]. */
fun Combinator.fullParses(value: String): Sequence<Parse> =
  parse(value, 0).filter { it.end == value.length }

/**
 * One lenient parse answers both questions the old two passes did:
 *  - syntactic ("could be this, color it"): did any path consume the whole value?
 *  - semantic ("actually valid"):           did any such path use only valid tokens?
 */
fun Combinator.validate(value: String): ParseOutcome {
  var firstBad: ParsedToken? = null
  for (p in fullParses(value)) {
    val bad = p.tokens.firstOrNull { !it.valid }
    if (bad == null) return ParseOutcome.Valid // short-circuit on the first fully-valid full parse
    if (firstBad == null) firstBad = bad
  }
  if (firstBad != null) return ParseOutcome.SemanticError(firstBad)
  val furthest = parse(value, 0).maxOfOrNull { it.end } ?: 0
  return ParseOutcome.SyntaxError(furthest)
}
