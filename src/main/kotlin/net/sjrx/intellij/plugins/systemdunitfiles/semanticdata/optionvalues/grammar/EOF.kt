package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

class EOF : Combinator {
  override fun SyntacticMatch(value: String, offset: Int): MatchResult {
    return if (offset == value.length) {
      MatchResult(emptyList(), offset, emptyList(), value.length)
    } else {
      NoMatch
    }
  }

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    return if (offset == value.length) {
      MatchResult(emptyList(), offset, emptyList(), value.length)
    } else {
      NoMatch
    }
  }

  override fun parse(value: String, offset: Int): Sequence<ParseStep> =
    if (offset == value.length) sequenceOf(Parse(offset, emptyList()))
    else sequenceOf(Stuck(offset, setOf(this))) // expected end-of-input here

  override fun toStringIndented(indent: Int): String {
    return "EOF"
  }
}
