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

  override fun parse(value: String, offset: Int): Sequence<ParseStep> {
    // Offer every choice that matches here (e.g. both ":" and "::"); each is always strictly valid.
    val matches = choices.filter { value.startsWith(it, offset) }
    return if (matches.isEmpty()) sequenceOf(Stuck(offset, setOf(this)))
    else matches.asSequence()
      .map { Parse(offset + it.length, listOf(ParsedToken(offset, offset + it.length, it, this, valid = true))) }
  }

  override fun toString(): String {
    return if (choices.size == 1) {
      "Literal(\"${choices[0]}\")"
    } else {
      "LitChoice(" + choices.joinToString(",") { "\"$it\"" } + ")"
    }
  }
}
