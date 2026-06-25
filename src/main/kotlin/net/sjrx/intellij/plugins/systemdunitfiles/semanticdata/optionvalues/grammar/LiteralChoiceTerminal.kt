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
    // Offer EVERY choice that matches at this offset, not just the longest. When choices share a
    // prefix more than one can match here -- e.g. CollectMode's ("inactive", "inactive-or-failed")
    // both match at the start of "inactive-or-failed". A greedy matcher would have to commit to one
    // and could dead-end later (taking "inactive" when the grammar needed "inactive-or-failed", or
    // vice versa); returning both as separate Parses lets the rest of the grammar pick the branch
    // that actually completes, with no backtracking. Every choice here is an exact literal, so each
    // matched token is always strictly valid (valid = true) -- the wrong-value case is what
    // FlexibleLiteralChoiceTerminal handles, where a token can match the shape but be valid = false.
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
