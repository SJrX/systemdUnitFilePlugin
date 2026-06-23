/*
 * ============================================================================
 *  Grammar engine — standalone proof of concept
 * ============================================================================
 *
 * A self-contained, dependency-free (Kotlin stdlib only) model of the option-value grammar engine
 * used by the systemd plugin. It mirrors the real classes in
 *   src/main/kotlin/.../semanticdata/optionvalues/grammar/
 * but with no IntelliJ types, so you can run and step through it without Gradle.
 *
 * Run it:
 *   kotlin poc/GrammarEnginePoc.kt
 * or:
 *   kotlinc poc/GrammarEnginePoc.kt -include-runtime -d /tmp/poc.jar && java -jar /tmp/poc.jar
 * or open it in IntelliJ and click Run on main().
 *
 * THE BIG IDEA — "list of successes"
 * ----------------------------------
 * Every matcher returns ALL the ways it can match, lazily:
 *
 *     fun parse(value, offset): Sequence<ParseStep>
 *
 * instead of one greedy first match. Seq threads each possibility of one part into the next, so a
 * value is valid if ANY path consumes the whole input. The classic trap Seq(ZeroOrMore("a"), "a")
 * on "aa" works, because ZeroOrMore offers the shorter match too (see demo 1).
 *
 * FAILURE IS A VALUE, NOT AN ABSENCE
 * ----------------------------------
 * A matcher that can't proceed returns a [Stuck] (carrying where it stuck and what it expected)
 * rather than an empty result. So "how far did we get / what was expected" rides back through the
 * return value — that single fact powers both error localization and completion (demos 2 and 3).
 */

// ---------------------------------------------------------------------------
//  Result types
// ---------------------------------------------------------------------------

/** A terminal token matched in the input, with the strict-validity verdict folded in. */
data class Token(val start: Int, val end: Int, val text: String, val valid: Boolean)

/** One step a matcher can take from an offset: it either consumed input ([Parse]) or got [Stuck]. */
sealed interface ParseStep

/** A successful match: consumed up to [end], producing [tokens] (each carrying its `valid` flag). */
data class Parse(val end: Int, val tokens: List<Token>) : ParseStep

/** A dead end at [offset]; [expected] is what the grammar was hoping to see there. */
data class Stuck(val offset: Int, val expected: Set<Matcher>) : ParseStep

/** A matcher is just a function from (input, offset) to every way it can proceed there. */
fun interface Matcher {
  fun parse(value: String, offset: Int): Sequence<ParseStep>
}

// ---------------------------------------------------------------------------
//  Terminals
// ---------------------------------------------------------------------------

/** An exact literal, e.g. Lit("none") or the "~" operator. Always strictly valid when it matches. */
data class Lit(val text: String) : Matcher {
  override fun parse(value: String, offset: Int): Sequence<ParseStep> =
    if (value.startsWith(text, offset)) {
      sequenceOf(Parse(offset + text.length, listOf(Token(offset, offset + text.length, text, valid = true))))
    } else {
      sequenceOf(Stuck(offset, setOf(this)))
    }

  override fun toString() = "\"$text\""
}

/**
 * Matches an identifier-shaped token loosely, but is only strictly `valid` if it is one of [choices].
 * "Lenient shape, strict membership": a wrong token like AF_BOGUS still matches (so we can locate it)
 * but is flagged invalid. [choices] is also what completion offers.
 */
class Choice(val choices: List<String>) : Matcher {
  constructor(vararg choices: String) : this(choices.toList())

  private val shape = Regex("[A-Za-z0-9_]+")

  override fun parse(value: String, offset: Int): Sequence<ParseStep> {
    val m = shape.matchAt(value, offset) ?: return sequenceOf(Stuck(offset, setOf(this)))
    val text = m.value
    return sequenceOf(Parse(offset + text.length, listOf(Token(offset, offset + text.length, text, text in choices))))
  }

  override fun toString() = "Choice$choices"
}

/** A maximal run of whitespace (a separator). */
object Whitespace : Matcher {
  override fun parse(value: String, offset: Int): Sequence<ParseStep> {
    var end = offset
    while (end < value.length && value[end].isWhitespace()) end++
    return if (end == offset) sequenceOf(Stuck(offset, setOf(this)))
    else sequenceOf(Parse(end, listOf(Token(offset, end, value.substring(offset, end), valid = true))))
  }

  override fun toString() = "<ws>"
}

// ---------------------------------------------------------------------------
//  Combinators
// ---------------------------------------------------------------------------

/** All parts in order. Threads each possibility of one part into the next; carries dead ends forward. */
class Seq(private vararg val parts: Matcher) : Matcher {
  override fun parse(value: String, offset: Int): Sequence<ParseStep> {
    var results: Sequence<ParseStep> = sequenceOf(Parse(offset, emptyList()))
    for (part in parts) {
      results = results.flatMap { acc ->
        when (acc) {
          is Stuck -> sequenceOf(acc)
          is Parse -> part.parse(value, acc.end).map { step ->
            when (step) {
              is Parse -> Parse(step.end, acc.tokens + step.tokens)
              is Stuck -> step
            }
          }
        }
      }
    }
    return results
  }
}

/** Any of the options — every option's steps are offered, so option order doesn't affect correctness. */
class Alt(private vararg val options: Matcher) : Matcher {
  override fun parse(value: String, offset: Int): Sequence<ParseStep> =
    options.asSequence().flatMap { it.parse(value, offset) }
}

/** Zero or one of [inner]: the empty match plus whatever [inner] offers. */
class ZeroOrOne(private val inner: Matcher) : Matcher {
  override fun parse(value: String, offset: Int): Sequence<ParseStep> =
    sequenceOf<ParseStep>(Parse(offset, emptyList())) + inner.parse(value, offset)
}

/**
 * Zero or more of [inner]. Offers EVERY repetition count (0, 1, 2, ...), not just the greedy maximum —
 * that completeness is what makes Seq(ZeroOrMore("a"), "a") match "aa". The `> from.end` guard stops
 * an inner matcher that can match empty from looping forever.
 */
class ZeroOrMore(private val inner: Matcher) : Matcher {
  override fun parse(value: String, offset: Int): Sequence<ParseStep> {
    fun extend(from: Parse): Sequence<ParseStep> = sequence {
      yield(from) // stop repeating here...
      for (step in inner.parse(value, from.end)) {
        when (step) {
          is Parse -> if (step.end > from.end) yieldAll(extend(Parse(step.end, from.tokens + step.tokens)))
          is Stuck -> yield(step)
        }
      }
    }
    return extend(Parse(offset, emptyList()))
  }
}

// ---------------------------------------------------------------------------
//  Capabilities — free functions over the parse result
// ---------------------------------------------------------------------------

sealed interface Outcome {
  object Valid : Outcome {
    override fun toString() = "Valid"
  }
  data class SemanticError(val badToken: Token) : Outcome
  data class SyntaxError(val furthest: Int, val expected: Set<Matcher>) : Outcome
}

/**
 * One lenient parse answers both questions: did any path consume the whole value (syntactic), and did
 * any such path use only valid tokens (semantic)? On failure the [Stuck] values fold into the deepest
 * offset reached and what was expected there.
 */
fun Matcher.validate(value: String): Outcome {
  var firstBad: Token? = null
  var furthest = 0
  var expected = emptySet<Matcher>()

  for (step in parse(value, 0)) {
    when (step) {
      is Parse -> {
        if (step.end == value.length) {
          val bad = step.tokens.firstOrNull { !it.valid }
          if (bad == null) return Outcome.Valid // first fully-valid full parse wins
          if (firstBad == null) firstBad = bad
        }
        if (step.end > furthest) { furthest = step.end; expected = emptySet() }
      }
      is Stuck -> when {
        step.offset > furthest -> { furthest = step.offset; expected = step.expected }
        step.offset == furthest -> expected = expected + step.expected
      }
    }
  }
  return firstBad?.let { Outcome.SemanticError(it) } ?: Outcome.SyntaxError(furthest, expected)
}

/** What literal/choice tokens could come next if the value typed so far is exactly [prefix]? */
fun Matcher.nextTokenChoices(prefix: String): Set<String> {
  val out = linkedSetOf<String>()
  for (step in parse(prefix, 0)) {
    if (step is Stuck && step.offset == prefix.length) {
      for (m in step.expected) when (m) {
        is Lit -> out += m.text
        is Choice -> out += m.choices
        else -> {}
      }
    }
  }
  return out
}

// ---------------------------------------------------------------------------
//  Demo
// ---------------------------------------------------------------------------

private fun heading(s: String) = println("\n=== $s ===")

fun main() {
  // ---- Demo 1: the greedy case the original single-path engine fails -------------------------
  heading("1. Completeness: Seq(ZeroOrMore(\"a\"), \"a\")")
  val greedy = Seq(ZeroOrMore(Lit("a")), Lit("a"))
  for (v in listOf("a", "aa", "aaa", "", "ab")) {
    println("  ${v.padEnd(4).ifBlank { "\"\"".padEnd(4) }} -> ${greedy.validate(v)}")
  }
  println("  (a single greedy match would eat both a's in \"aa\" and reject it; we offer the shorter match)")

  // ---- A small RestrictAddressFamilies-style grammar -----------------------------------------
  // none | [~] family (whitespace family)*
  val family = Choice("AF_INET", "AF_INET6", "AF_UNIX", "AF_NETLINK", "AF_PACKET")
  val addressFamilies: Matcher = Alt(
    Lit("none"),
    Seq(ZeroOrOne(Lit("~")), family, ZeroOrMore(Seq(Whitespace, family))),
  )

  heading("2. Validation + error localization")
  for (v in listOf("none", "AF_INET AF_INET6", "~AF_PACKET", "AF_BOGUS", "AF_INET, AF_INET6")) {
    println("  ${v.padEnd(20)} -> ${addressFamilies.validate(v)}")
  }
  println("  (AF_BOGUS: well-formed but invalid -> SemanticError; the comma: SyntaxError at the stuck offset)")

  // ---- Demo 3: completion is the same 'expected set' question --------------------------------
  heading("3. Completion: what can come next?")
  for (prefix in listOf("", "~", "AF_INET ")) {
    println("  after \"${prefix}\" -> ${addressFamilies.nextTokenChoices(prefix)}")
  }
  println("  (to complete a partial like \"AF_IN\", you parse the prefix BEFORE it and filter by \"AF_IN\")")

  // ---- Demo 4: peek at the raw step stream (what you'd see in a debugger) ---------------------
  heading("4. Raw parse steps for \"AF_INET, AF_INET6\"")
  addressFamilies.parse("AF_INET, AF_INET6", 0).forEach { println("  $it") }
}
