package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

class  LiteralChoiceTerminal(vararg var choices: String) : TerminalCombinator {

  init {
    choices.sortBy { -it.length }
  }

  private fun match(value: String, offset: Int): MatchResult {
    for (choice in choices) {
      if (value.substring(offset).startsWith(choice)) {
        return MatchResult(listOf(choice), offset + choice.length, listOf(this), offset + choice.length)
      }
    }
    return NoMatch
  }


  override fun SyntacticMatch(value: String, offset: Int): MatchResult {
    return match(value, offset)
  }

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    return match(value, offset)
  }
}
