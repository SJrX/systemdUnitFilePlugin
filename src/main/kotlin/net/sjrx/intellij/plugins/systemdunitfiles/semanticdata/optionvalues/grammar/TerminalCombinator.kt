package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

interface TerminalCombinator : Combinator {
  override fun toStringIndented(indent: Int): String {
    return toString()
  }

  /**
   * A message if [token] (a value this terminal matched) is valid but deprecated, else null.
   * Lets a grammar flag accepted-but-obsolete values (e.g. kernel-removed address families).
   */
  fun deprecationFor(token: String): String? = null
}
