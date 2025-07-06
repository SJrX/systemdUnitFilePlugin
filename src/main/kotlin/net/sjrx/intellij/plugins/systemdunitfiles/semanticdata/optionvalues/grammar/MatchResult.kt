package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange

data class Range(val start: Int, val end: Int)

data class Highlight(val start: Int, val end: Int, )

data class AstNode(val type: String, val text: String, val children: List<AstNode> = emptyList())
// One day we should remove the dependencies on intellij. Maybe change the grammar to build an AST.

data class MatchResult(val tokens: List<String>, val matchResult: Int, val terminals: List<TerminalCombinator>, val longestMatch : Int, val astNode : AstNode? = null) {
  init {
    if (tokens.size != terminals.size) {
      throw IllegalArgumentException("Tokens and terminals must be the same size, ${tokens.size} != ${terminals.size}")
    }
  }
}

val NoMatch = MatchResult(emptyList(), -1, emptyList(), 0)
