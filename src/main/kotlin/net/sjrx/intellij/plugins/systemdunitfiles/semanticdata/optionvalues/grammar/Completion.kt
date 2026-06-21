package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/*
 * Grammar-based completion support (GitHub #467 / #343).
 *
 * The frontier we built for error localization is exactly what completion needs: "what was expected
 * at this offset?" is the same question as "what could come next here?". [nextTokenChoices] answers
 * it for the position at the end of [prefix].
 *
 * It reads the FIRST set at the end of [prefix] from the parse frontier — the Stuck values whose
 * offset is the end of [prefix] — and collects the enumerable choices of the terminals expected
 * there (literal choices and flexible-literal choices). Non-enumerable terminals (numbers, regexes,
 * whitespace, EOF) contribute nothing to suggest.
 *
 * Pure Kotlin, no IntelliJ types. The matcher is exhaustive, so [maxSteps] bounds the work; callers
 * running on a UI/highlighting thread should also poll cancellation between calls.
 */
fun Combinator.nextTokenChoices(prefix: String, maxSteps: Int = 100_000): Set<String> {
  val choices = linkedSetOf<String>()
  var steps = 0
  for (step in parse(prefix, 0)) {
    if (++steps > maxSteps) break
    if (step is Stuck && step.offset == prefix.length) {
      for (matcher in step.expected) {
        when (matcher) {
          is LiteralChoiceTerminal -> choices += matcher.choices
          is FlexibleLiteralChoiceTerminal -> choices += matcher.choices
          else -> {} // not enumerable — nothing concrete to suggest
        }
      }
    }
  }
  return choices
}
