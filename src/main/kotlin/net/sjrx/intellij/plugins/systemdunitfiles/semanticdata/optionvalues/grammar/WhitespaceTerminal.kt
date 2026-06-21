package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

class WhitespaceTerminal : TerminalCombinator {

  private fun match(value: String, offset: Int): MatchResult {
    var newOffset = offset
    for (o in offset until value.length) {
      if (value[o].isWhitespace()) {
        newOffset = o + 1
      } else {
        break
      }
    }

    if (newOffset == offset) {
      return NoMatch.copy(longestMatch = offset)
    }

    return MatchResult(listOf(value.substring(offset, newOffset)), newOffset, listOf(this), newOffset)
  }

  override fun SyntacticMatch(value: String, offset: Int): MatchResult {
    return match(value, offset)
  }

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    return match(value, offset)
  }

  override fun parse(value: String, offset: Int): Sequence<Parse> {
    var end = offset
    while (end < value.length && value[end].isWhitespace()) end++
    return if (end == offset) emptySequence()
    else sequenceOf(Parse(end, listOf(ParsedToken(offset, end, value.substring(offset, end), this, valid = true))))
  }

  override fun toString(): String {
    return "\\s+"
  }

  override fun toStringIndented(indent: Int): String {
    return toString()
  }
}
