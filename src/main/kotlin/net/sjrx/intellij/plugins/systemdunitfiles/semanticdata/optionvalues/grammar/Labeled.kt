package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/**
 * Wraps [inner] and tags the whole matched span with a coloring [role] (#467 / #342).
 *
 * It is OPTIONAL and TRANSPARENT: matching (SyntacticMatch / SemanticMatch / parse) is delegated to
 * [inner] unchanged, so wrapping a sub-grammar affects only coloring, never validation or
 * completion. Use it where a composite value should read as one unit — e.g.
 * `Labeled(Role.LITERAL, IPV4_ADDR)` colors `127.0.0.1` as a single literal instead of per-octet.
 */
class Labeled(private val role: Role, private val inner: Combinator) : Combinator {

  override fun SyntacticMatch(value: String, offset: Int): MatchResult = inner.SyntacticMatch(value, offset)

  override fun SemanticMatch(value: String, offset: Int): MatchResult = inner.SemanticMatch(value, offset)

  override fun parse(value: String, offset: Int): Sequence<ParseStep> =
    inner.parse(value, offset).map { step ->
      when (step) {
        is Parse ->
          if (step.end > offset) Parse(step.end, step.tokens, step.regions + Region(offset, step.end, role))
          else step // matched nothing; no region to add
        is Stuck -> step
      }
    }

  override fun toStringIndented(indent: Int): String = inner.toStringIndented(indent)
}
