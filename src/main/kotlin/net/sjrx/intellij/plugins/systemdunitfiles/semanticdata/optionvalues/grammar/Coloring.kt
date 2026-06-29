package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/*
 * Grammar-based syntax coloring (#467 / #342).
 *
 * Coloring is OPTIONAL and mostly automatic: [colorize] assigns each matched token a [Role] from
 * its terminal's [defaultRole], so existing grammars are coloured with no changes. Where a composite
 * span should read as one unit (e.g. an IP address rather than per-octet), wrap it in [Labeled],
 * which paints the whole span with one role and is otherwise transparent to matching.
 *
 * Roles are abstract; the role -> TextAttributes mapping lives in the IntelliJ layer.
 */

/** Semantic role of a coloured span. */
enum class Role {
  /** A value chosen from a fixed set of words (e.g. `none`, `verity`, `AF_INET`). */
  ENUM,

  /** A literal value: a number, or a composite value span wrapped in [Labeled] (e.g. an IP). */
  LITERAL,

  /** A punctuation separator/operator (e.g. `:`, `+`, `=`, `~`, `/`). */
  OPERATOR,
}

/** A coloured span `[start, end)` and its [role]. */
data class Region(val start: Int, val end: Int, val role: Role)

/**
 * The role a terminal should get when it is NOT wrapped in [Labeled]. `null` means "do not colour"
 * (whitespace, and anything we don't recognise).
 */
fun defaultRole(terminal: TerminalCombinator): Role? = when (terminal) {
  is IntegerTerminal -> Role.LITERAL
  is LiteralChoiceTerminal -> if (terminal.choices.allPunctuation()) Role.OPERATOR else Role.ENUM
  is FlexibleLiteralChoiceTerminal -> if (terminal.choices.allPunctuation()) Role.OPERATOR else Role.ENUM
  // RegexTerminal (free-form names/strings: Description=, interface names, ...) and whitespace stay
  // uncoloured by default — they keep the editor's normal value colour.
  else -> null
}

private fun Array<out String>.allPunctuation(): Boolean =
  isNotEmpty() && all { choice -> choice.isNotEmpty() && choice.none(Char::isLetterOrDigit) }

/**
 * The explicit [Labeled] spans in [value] (e.g. a whole IP address), from the first fully-valid
 * parse — i.e. structure the grammar marked, without the per-token coloring defaults. Used by
 * features that act on semantic spans, such as IPv6 canonicalization.
 */
fun Combinator.labeledRegions(value: String): List<Region> {
  val parse = parse(value, 0).filterIsInstance<Parse>()
    .firstOrNull { it.end == value.length && it.tokens.all { token -> token.valid } } ?: return emptyList()
  return parse.regions
}

/**
 * The coloured regions for [value]. Explicit [Labeled] regions win; any token not inside a labeled
 * region gets its terminal's [defaultRole]. Returns empty if no full parse exists — we don't colour
 * values that don't match the grammar.
 */
fun Combinator.colorize(value: String): List<Region> {
  val parse = parse(value, 0).filterIsInstance<Parse>().firstOrNull { it.end == value.length } ?: return emptyList()

  val regions = parse.regions.toMutableList()
  for (token in parse.tokens) {
    val role = defaultRole(token.terminal) ?: continue
    if (regions.none { token.start >= it.start && token.end <= it.end }) {
      regions.add(Region(token.start, token.end, role))
    }
  }
  // distinct(): nested Labeled (e.g. an IPv4 suffix inside an IPv6 address) can emit the same span
  // twice; collapse exact duplicates.
  return regions.distinct().sortedBy { it.start }
}
