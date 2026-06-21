package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar2

/*
 * ============================================================================
 *  Grammar engine PoC  (GitHub #467, step 2)
 * ============================================================================
 *
 * This is a parallel, self-contained proof of concept for a new option-value
 * grammar engine. It does NOT replace the existing `..optionvalues.grammar`
 * package yet; it sits beside it so we can play with the idea and validate it
 * against the existing behavioural tests before committing.
 *
 * THE ONE IDEA
 * ------------
 * Today every combinator returns *one* answer (greedy, first match, no
 * backtracking), so `Seq(ZeroOrMore("a"), "a")` fails to match "aa": the star
 * eats both a's and the trailing "a" has nothing left.
 *
 * Here every matcher instead returns *all the ways it could match*, lazily:
 *
 *     fun parse(input, offset): Sequence<Parse>
 *
 * `Seq` then threads each possibility of one part into the next, and a value is
 * valid if *any* path consumes the whole string. The star "gives back" an `a`
 * for free, simply because it OFFERED the shorter match as one of its results.
 * This is Wadler's "list of successes". Laziness means we explore depth-first
 * and stop at the first complete parse, so we pay only for what we use.
 *
 * Two more things fall out of carrying a little structure on each result:
 *   - a *labeled* parse tree (a `Branch` with a role) — so an IPv4 address is
 *     ONE labeled span, not blue-octet/black-dot/blue-octet. Coloring,
 *     deprecation warnings and canonicalization all become free functions that
 *     walk this tree.
 *   - per-leaf *validity* flags, so the old "syntactic vs semantic" two passes
 *     collapse into one lenient parse: a token can match (so we can color /
 *     locate it) while still being flagged invalid.
 *
 * No IntelliJ types appear in this file on purpose: the engine stays pure
 * Kotlin and speaks plain Int offsets, and the IntelliJ layer adapts later.
 */

/** A semantic role attached to a labeled span. Drives coloring / warnings / rewrites. */
enum class Role {
  KEYWORD,        // a fixed literal like "none" or the "~" inversion prefix
  ADDRESS_FAMILY, // an AF_* name
  WHITESPACE,
}

/** A node in the parse tree produced for one specific input. */
sealed interface Node {
  val start: Int
  val end: Int
}

/** A terminal that matched the text `input[start until end]`. `valid` is the strict check. */
data class Leaf(
  override val start: Int,
  override val end: Int,
  val text: String,
  val role: Role?,
  val valid: Boolean,
) : Node

/** A labeled grouping of child nodes — this is what gives the tree its shape. */
data class Branch(
  val role: Role,
  override val start: Int,
  override val end: Int,
  val children: List<Node>,
) : Node

/** One successful way a matcher consumed input: it ended at `end` and produced `nodes`. */
data class Parse(val end: Int, val nodes: List<Node>)

/** A matcher is just a function from (input, offset) to every way it can match there. */
fun interface Matcher {
  /** Empty sequence == no match. Otherwise, one `Parse` per distinct way to match. */
  fun parse(input: String, offset: Int): Sequence<Parse>
}

// ---------------------------------------------------------------------------
//  Terminals
// ---------------------------------------------------------------------------

/** Matches an exact string, e.g. Lit("none") or Lit("~"). Always strictly valid when it matches. */
class Lit(private val text: String, private val role: Role? = Role.KEYWORD) : Matcher {
  override fun parse(input: String, offset: Int): Sequence<Parse> =
    if (input.startsWith(text, offset)) {
      sequenceOf(Parse(offset + text.length, listOf(Leaf(offset, offset + text.length, text, role, valid = true))))
    } else {
      emptySequence()
    }
}

/**
 * Matches an identifier-shaped token loosely, then flags it valid only if it is an exact choice.
 * This is the "lenient shape, strict membership" trick: a bad token like AF_BOGUS still matches
 * (so we can highlight exactly it), but is reported invalid.
 */
class FlexibleChoice(
  private val choices: Set<String>,
  private val role: Role? = null,
  private val shape: Regex = Regex("[A-Za-z0-9_]+"),
) : Matcher {
  constructor(vararg choices: String, role: Role? = null) : this(choices.toSet(), role)

  override fun parse(input: String, offset: Int): Sequence<Parse> {
    val m = shape.matchAt(input, offset) ?: return emptySequence()
    val text = m.value
    return sequenceOf(Parse(offset + text.length, listOf(Leaf(offset, offset + text.length, text, role, text in choices))))
  }
}

/** Matches a maximal run of whitespace (a separator). */
object Whitespace : Matcher {
  override fun parse(input: String, offset: Int): Sequence<Parse> {
    var end = offset
    while (end < input.length && input[end].isWhitespace()) end++
    return if (end == offset) emptySequence()
    else sequenceOf(Parse(end, listOf(Leaf(offset, end, input.substring(offset, end), Role.WHITESPACE, valid = true))))
  }
}

// ---------------------------------------------------------------------------
//  Combinators
// ---------------------------------------------------------------------------

/** All parts in order. Threads each possibility of one part into the next (the cartesian product). */
class Seq(private vararg val parts: Matcher) : Matcher {
  override fun parse(input: String, offset: Int): Sequence<Parse> {
    var results = sequenceOf(Parse(offset, emptyList()))
    for (part in parts) {
      results = results.flatMap { acc ->
        part.parse(input, acc.end).map { next -> Parse(next.end, acc.nodes + next.nodes) }
      }
    }
    return results
  }
}

/** Any of the options. Yields *all* options' matches concatenated, so ordering no longer matters. */
class Alt(private vararg val options: Matcher) : Matcher {
  override fun parse(input: String, offset: Int): Sequence<Parse> =
    options.asSequence().flatMap { it.parse(input, offset) }
}

/** Zero or one of `inner`. Offers both the empty match and `inner`'s matches. */
class ZeroOrOne(private val inner: Matcher) : Matcher {
  override fun parse(input: String, offset: Int): Sequence<Parse> =
    sequenceOf(Parse(offset, emptyList())) + inner.parse(input, offset)
}

/**
 * Zero or more of `inner`. Crucially this offers EVERY repetition count (0, 1, 2, ...), not just
 * the greedy maximum — that is what makes matching complete. The `> from.end` guard keeps an
 * inner matcher that can match empty from looping forever.
 */
class ZeroOrMore(private val inner: Matcher) : Matcher {
  override fun parse(input: String, offset: Int): Sequence<Parse> {
    fun extend(from: Parse): Sequence<Parse> = sequence {
      yield(from) // stop repeating here...
      for (step in inner.parse(input, from.end)) {
        if (step.end > from.end) {
          yieldAll(extend(Parse(step.end, from.nodes + step.nodes))) // ...or take one more and recurse
        }
      }
    }
    return extend(Parse(offset, emptyList()))
  }
}

/** Wraps `inner` and collapses everything it matched into a single labeled `Branch`. */
class Labeled(private val role: Role, private val inner: Matcher) : Matcher {
  override fun parse(input: String, offset: Int): Sequence<Parse> =
    inner.parse(input, offset).map { p ->
      val end = p.nodes.lastOrNull()?.end ?: offset
      Parse(p.end, listOf(Branch(role, offset, end, p.nodes)))
    }
}

// ---------------------------------------------------------------------------
//  Capabilities — free functions over the parse result, no combinator code
// ---------------------------------------------------------------------------

/** Flatten a parse tree to its leaves, in source order. */
fun Parse.leaves(): List<Leaf> = nodes.flatMap { it.leaves() }

private fun Node.leaves(): List<Leaf> = when (this) {
  is Leaf -> listOf(this)
  is Branch -> children.flatMap { it.leaves() }
}

/** The outcome of validating a value against a grammar. */
sealed interface Outcome {
  /** Some path consumed the whole input with every token strictly valid. */
  object Valid : Outcome
  /** A path consumed the whole input, but a token is not strictly valid (well-formed but wrong). */
  data class SemanticError(val badToken: Leaf) : Outcome
  /** No path consumed the whole input. `furthest` is how far we got (for error localization). */
  data class SyntaxError(val furthest: Int) : Outcome
}

/**
 * Validate `input` against `grammar`, requiring the whole string to be consumed. One lenient parse
 * answers both questions: syntactic well-formedness (did any path reach the end?) and semantic
 * validity (did any such path have only valid tokens?).
 */
fun validate(grammar: Matcher, input: String): Outcome {
  var firstBad: Leaf? = null
  for (p in grammar.parse(input, 0)) {
    if (p.end != input.length) continue // not a full match — ignore for validity
    val bad = p.leaves().firstOrNull { !it.valid }
    if (bad == null) return Outcome.Valid // short-circuit on the first fully-valid full parse
    if (firstBad == null) firstBad = bad
  }
  if (firstBad != null) return Outcome.SemanticError(firstBad)
  // Nothing reached the end. Report the furthest offset any partial path reached.
  val furthest = grammar.parse(input, 0).maxOfOrNull { it.end } ?: 0
  return Outcome.SyntaxError(furthest)
}
