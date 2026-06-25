package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/*
 * List-of-successes matcher (GitHub #467).
 *
 * These types support a second matching method, `Combinator.parse()`, that lives ALONGSIDE the
 * existing SyntacticMatch / SemanticMatch on every combinator. Nothing here is wired into
 * GrammarOptionValue yet — the goal is to flesh the approach out on the real combinators and
 * validate it against the real grammars in tests before deciding to migrate the caller.
 *
 * Where the existing engine returns ONE greedy result and runs two near-identical passes, parse()
 * returns EVERY way a combinator can proceed (lazily), and folds the strict "semantic" check into a
 * `valid` flag on each token. So one lenient pass answers both questions, and greedy traps like
 * Seq(ZeroOrMore("a"), "a") on "aa" resolve themselves.
 *
 * FAILURE IS A VALUE, NOT AN ABSENCE
 * ----------------------------------
 * A matcher does not signal "no match" by returning an empty sequence. It returns a [Stuck] — a
 * first-class value carrying the offset it got stuck at and what it was hoping to see. That single
 * decision is why error localization needs no side-channel: when Seq(..., EOF()) can't finish, the
 * EOF failure rides back up the return value as a Stuck(offset=7, {EOF}), so we still know we
 * reached offset 7. (Earlier this was modelled as an empty sequence, which threw the offset away and
 * forced a mutable "frontier" object to be threaded through parse() to recover it.)
 *
 * SIMPLER ALTERNATIVE (for the record): instead of returning Stuck values, you can thread a mutable
 * accumulator ("frontier") through parse() that every leaf matcher writes its deepest reach into.
 * That is less code and a touch lazier, but it splits the data flow across two channels — successes
 * via the return value, failures via a pass-by-reference side effect — which is the asymmetry this
 * design removes by making both kinds of result travel the same way.
 */

/** A single terminal token, with the strict-validity verdict (the old "semantic" check) folded in. */
data class ParsedToken(
  val start: Int,
  val end: Int,
  val text: String,
  val terminal: TerminalCombinator,
  val valid: Boolean,
)

/** One step a matcher can take from an offset: either it consumed input ([Parse]) or it got [Stuck]. */
sealed interface ParseStep

/** A successful match: consumed input up to [end], producing [tokens] (each with its `valid` flag). */
data class Parse(val end: Int, val tokens: List<ParsedToken>) : ParseStep

/**
 * A dead end: matching could not proceed at [offset], where [expected] is the set of matchers the
 * grammar was hoping to see. Carrying this as a value (rather than an empty result) is what lets us
 * localize errors and, later, drive completion — both are "what was expected at this offset?".
 */
data class Stuck(val offset: Int, val expected: Set<Combinator>) : ParseStep

/** The outcome of validating a whole value against a grammar via parse(). */
sealed interface ParseOutcome {
  /** Some path consumed the whole value with every token strictly valid. */
  object Valid : ParseOutcome

  /** A path consumed the whole value, but a token is not strictly valid (well-formed but wrong). */
  data class SemanticError(val badToken: ParsedToken) : ParseOutcome

  /**
   * No path consumed the whole value. [furthest] is the deepest offset any path reached, and
   * [expected] is what the grammar was hoping to see there (for error localization / completion).
   */
  data class SyntaxError(val furthest: Int, val expected: Set<Combinator>) : ParseOutcome
}

/**
 * One lenient parse answers both questions the old two passes did:
 *  - syntactic ("could be this, color it"): did any path consume the whole value?
 *  - semantic ("actually valid"):           did any such path use only valid tokens?
 *
 * On failure we fold the [Stuck] values back into the deepest offset reached and the union of what
 * was expected there — the "frontier", computed from the return value rather than mutated into it.
 *
 * The matcher is exhaustive, so a pathologically ambiguous grammar could explore a huge number of
 * steps. Two pure guards keep this safe to run on a UI/highlighting thread without any IntelliJ
 * dependency here: [onStep] is invoked once per explored step (the IntelliJ layer passes a callback
 * that throws on cancellation), and [maxSteps] caps total work. If the cap is hit we fail OPEN —
 * return [ParseOutcome.Valid] rather than flag a value we could not fully explore.
 *
 * When the value is well-formed but invalid, an ambiguous grammar can yield several full parses that
 * tokenize the same string differently, each with a different first-invalid token. We do NOT report
 * whichever the lazy stream happens to yield first (that would depend on incidental combinator
 * iteration order). Instead we report the invalid token from the parse that stayed valid the LONGEST
 * — the largest start offset — mirroring how a [ParseOutcome.SyntaxError] reports the furthest offset
 * reached. That rule is invariant under combinator iteration order. The only remaining tie is two
 * parses whose first-invalid token starts at the very same offset; there the earlier one in stream
 * order wins. For a tie produced by an [AlternativeCombinator] (e.g. two enums over the same
 * character shape) that is the earlier-declared branch, so an author can steer it by ordering; ties
 * from other combinators follow that combinator's own order ([LiteralChoiceTerminal] longest-first,
 * the repetition combinators shorter-count-first).
 */
fun Combinator.validate(value: String, maxSteps: Int = 1_000_000, onStep: () -> Unit = {}): ParseOutcome {
  var deepestBad: ParsedToken? = null
  var furthest = 0
  var expected = emptySet<Combinator>()
  var steps = 0

  for (step in parse(value, 0)) {
    onStep()
    if (++steps > maxSteps) return ParseOutcome.Valid
    when (step) {
      is Parse -> {
        if (step.end == value.length) {
          val bad = step.tokens.firstOrNull { !it.valid }
          if (bad == null) return ParseOutcome.Valid // any fully-valid full parse wins; short-circuit
          // Keep the bad token from the parse that stayed valid the longest; an exact tie keeps the
          // first in stream order. See the function doc for why this is order-invariant.
          val current = deepestBad
          if (current == null || bad.start > current.start) deepestBad = bad
        }
        if (step.end > furthest) { furthest = step.end; expected = emptySet() }
      }
      is Stuck -> when {
        step.offset > furthest -> { furthest = step.offset; expected = step.expected }
        step.offset == furthest -> expected = expected + step.expected
      }
    }
  }

  return deepestBad?.let { ParseOutcome.SemanticError(it) } ?: ParseOutcome.SyntaxError(furthest, expected)
}
