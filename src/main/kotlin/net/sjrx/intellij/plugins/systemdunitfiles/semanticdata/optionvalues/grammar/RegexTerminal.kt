package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

class RegexTerminal(syntaticMatchStr : String, semanticMatchStr: String ) : TerminalCombinator {

  val syntaticMatch = syntaticMatchStr.toRegex()
  val semanticMatch = semanticMatchStr.toRegex()

  override fun SyntacticMatch(value: String, offset: Int): MatchResult {
    val matchResult = syntaticMatch.matchAt(value, offset) ?: return NoMatch

    return MatchResult(listOf(matchResult.value), offset + matchResult.value.length, listOf(this), offset + matchResult.value.length)

  }

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    val matchResult = semanticMatch.matchAt(value, offset)  ?: return NoMatch

    return MatchResult(listOf(matchResult.value), offset + matchResult.value.length, listOf(this), offset + matchResult.value.length)
  }

  override fun parse(value: String, offset: Int): Sequence<ParseStep> {
    // The syntactic regex gives the lenient span; valid iff the semantic regex matches that same span.
    val syn = syntaticMatch.matchAt(value, offset) ?: return sequenceOf(Stuck(offset, setOf(this)))
    val text = syn.value
    val valid = semanticMatch.matchAt(value, offset)?.value == text
    return sequenceOf(Parse(offset + text.length, listOf(ParsedToken(offset, offset + text.length, text, this, valid))))
  }

  override fun toString(): String {
    return "Regex(\"${semanticMatch.pattern}\")"
  }
}
