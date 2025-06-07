package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

interface TerminalCombinator : Combinator {
  override fun toStringIndented(indent: Int): String {
    return toString()
  }
}
