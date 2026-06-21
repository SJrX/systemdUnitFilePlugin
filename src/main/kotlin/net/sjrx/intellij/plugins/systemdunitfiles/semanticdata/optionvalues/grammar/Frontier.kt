package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/**
 * The "frontier" — a high-water mark recorder threaded through parse() (#467 step 3).
 *
 * parse() only ever returns *successful* matches, so when a value is malformed the failing paths
 * vanish and we lose all trace of how far we got. The frontier is a side-channel that survives
 * failure: every leaf matcher (a terminal, or EOF) reports itself here when it is consulted at an
 * offset, and the frontier keeps only the DEEPEST offset reached and the set of matchers wanted
 * there.
 *
 * That gives two things from one mechanism:
 *  - error localization: the deepest offset is where parsing got stuck, and `expected` is what
 *    would have been valid there;
 *  - the seed of completion (#343): "what could come next at this position?" is the same question.
 *
 * It is mutable and shared across the whole (lazy) exploration of a single value on purpose — it is
 * the global deepest-reach across every path tried.
 */
class Frontier {
  /** The deepest offset at which any leaf matcher was consulted. */
  var position: Int = 0
    private set

  private val expectedAtPosition = linkedSetOf<Combinator>()

  /** The matchers consulted at [position] — i.e. what the grammar was hoping to see there. */
  val expected: Set<Combinator> get() = expectedAtPosition

  /** Record that [matcher] was consulted at [offset]. Only the deepest offset's matchers are kept. */
  fun reached(offset: Int, matcher: Combinator) {
    when {
      offset > position -> {
        position = offset
        expectedAtPosition.clear()
        expectedAtPosition.add(matcher)
      }
      offset == position -> expectedAtPosition.add(matcher)
      // offset < position: a shallower path, ignore.
    }
  }
}
