package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange

class HighlightCombinator(val combinator: Combinator, val attributeKey : TextAttributesKey ) : Combinator {

  override fun SyntacticMatch(value: String, offset: Int): MatchResult {
    val startOffset = offset
    val match = combinator.SyntacticMatch(value, offset)

    if (match.matchResult >= 0) {
      // Create a new mutable map from the existing one
      val newHighlights = match.highlights.toMutableMap()

      // Add your new highlight
      newHighlights[TextRange(startOffset, startOffset + match.matchResult)] = attributeKey

      // Create a new MatchResult with the updated highlights map
      return match.copy(highlights = newHighlights)

    }

    return match
  }

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    return combinator.SemanticMatch(value, offset)
  }

  override fun toStringIndented(indent: Int): String {
    TODO("Not yet implemented")
  }
}
