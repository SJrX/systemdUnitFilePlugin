package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import kotlin.math.max

class FlexibleLiteralChoiceTerminal(vararg val choices: String) : TerminalCombinator {

  init {
    choices.sortBy { -it.length }
  }

  val syntaticMatch: Regex

  init {

    var dash = false
    var specialChars = ""
    var lowerCase = false
    var upperCase = false
    var numbers = false
    var maxLength = 0

    for (choice in choices) {
      maxLength = max(maxLength, choice.length)
      for (char in choice) {
        if (char in 'a'..'z') {
          lowerCase = true
          continue
        }

        if (char in 'A'..'Z') {
          upperCase = true
          continue
        }

        if (char in '0'..'9') {
          numbers = true
          continue
        }

        if (char == '-') {
          dash = true
          continue
        }

        specialChars += char
      }
    }

    var regexClass = ""

    if (lowerCase) {
      regexClass += "a-z"
    }

    if (upperCase) {
      regexClass += "A-Z"
    }

    if (numbers) {
      regexClass += "0-9"
    }

    regexClass += specialChars

    if (dash) {
      regexClass += "-"
    }

    syntaticMatch = ("[" + regexClass + "]{1,$maxLength}").toRegex()
  }


  override fun SyntacticMatch(value: String, offset: Int): MatchResult {
    for (choice in choices) {
      if (value.substring(offset).startsWith(choice)) {
        return MatchResult(listOf(choice), offset + choice.length, listOf(this), offset + choice.length)
      }
    }

    val matchResult = syntaticMatch.matchAt(value, offset) ?: return NoMatch

    return MatchResult(listOf(matchResult.value), offset + matchResult.value.length, listOf(this), offset + matchResult.value.length)
  }

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    for (choice in choices) {
      if (value.substring(offset).startsWith(choice)) {
        return MatchResult(listOf(choice), offset + choice.length, listOf(this), offset + choice.length)
      }
    }
    return NoMatch.copy(longestMatch = offset)
  }

  override fun parse(value: String, offset: Int, frontier: Frontier): Sequence<Parse> {
    frontier.reached(offset, this)
    // Lenient shape match (so a wrong token like AF_BOGUS still matches and can be highlighted),
    // valid only if the matched text is one of the exact choices.
    val m = syntaticMatch.matchAt(value, offset) ?: return emptySequence()
    val text = m.value
    val valid = choices.any { it == text }
    return sequenceOf(Parse(offset + text.length, listOf(ParsedToken(offset, offset + text.length, text, this, valid))))
  }

  override fun toString(): String {
    return if (choices.size == 1) {
      "Literal(\"${choices[0]}\")"
    } else {
      "FlexLitChoice(" + choices.joinToString(",") { "\"$it\"" } + ")"
    }
  }

  override fun toStringIndented(indent: Int): String {
    return toString()
  }
}
