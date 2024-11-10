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
}
