package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

class OptionalWhitespacePrefix(val combinator: Combinator):
  AlternativeCombinator(
    SequenceCombinator(WhitespaceTerminal(), combinator),
    combinator
  ) {
//
//  override fun SyntacticMatch(value: String,  offset: Int): MatchResult {
//    var newOffset = offset
//    for(o in offset..<value.length) {
//      if (value[o].isWhitespace()) {
//        newOffset = o + 1
//      } else {
//        break
//      }
//    }
//
//    return combinator.SyntacticMatch(value, newOffset)
//  }
//
//  override fun SemanticMatch(value: String, offset: Int): MatchResult {
//    var newOffset = offset
//    for(o in offset..<value.length) {
//      if (value[o].isWhitespace()) {
//        newOffset = o + 1
//      } else {
//        break
//      }
//    }
//
//    return combinator.SemanticMatch(value, newOffset)
//  }
}
