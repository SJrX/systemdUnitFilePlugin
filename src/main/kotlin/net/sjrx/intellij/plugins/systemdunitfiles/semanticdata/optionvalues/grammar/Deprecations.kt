package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/*
 * Valid-but-deprecated value detection (#467). A reusable companion to validation: a value can be
 * perfectly valid yet use an obsolete token (e.g. an address family the kernel removed). Terminals
 * declare which of their matched values are deprecated via [TerminalCombinator.deprecationFor].
 */

/** A deprecated token at `[start, end)` in the value, with the reason to show. */
data class DeprecatedToken(val start: Int, val end: Int, val message: String)

/**
 * Deprecated tokens in [value], taken from the first full parse. Empty if the value doesn't fully
 * parse (an invalid value is the InvalidValue inspection's job; deprecation is reported once valid).
 */
fun Combinator.deprecatedTokens(value: String): List<DeprecatedToken> {
  // Require a fully-valid parse: don't pile a deprecation note on top of an otherwise invalid value.
  val parse = parse(value, 0).filterIsInstance<Parse>()
    .firstOrNull { it.end == value.length && it.tokens.all { token -> token.valid } } ?: return emptyList()
  return parse.tokens.mapNotNull { token ->
    token.terminal.deprecationFor(token.text)?.let { DeprecatedToken(token.start, token.end, it) }
  }
}
