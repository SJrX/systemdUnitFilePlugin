package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

class OptionalWhitespacePrefix(val combinator: Combinator):
  AlternativeCombinator(
    SequenceCombinator(WhitespaceTerminal(), combinator),
    combinator
  ) {


  override fun toString(): String {
    return "\\s*{${combinator}}"
  }
}
