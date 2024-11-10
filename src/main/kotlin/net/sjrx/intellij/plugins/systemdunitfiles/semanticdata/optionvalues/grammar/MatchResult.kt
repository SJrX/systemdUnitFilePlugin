package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

data class MatchResult(val tokens: List<String>, val matchResult: Int, val terminals: List<TerminalCombinator>, val longestMatch : Int ) {
  init {
    if (tokens.size != terminals.size) {
      throw IllegalArgumentException("Tokens and terminals must be the same size, ${tokens.size} != ${terminals.size}")
    }
  }
}

val NoMatch = MatchResult(emptyList(), -1, emptyList(), 0)
