package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the list-of-successes matcher `Combinator.parse()` / `validate()` (#467).
 *
 * These exercise the engine against grammars built from the SAME combinator classes the production
 * validators use — nothing in the 200+ grammar definitions changed; we just grew a second matching
 * method on the same combinators. The grammars here are hand-built so the expected outcomes are
 * unambiguous and independent of any particular production grammar.
 */
class ParseTest {

  private fun isValid(grammar: Combinator, value: String) = grammar.validate(value) == ParseOutcome.Valid

  @Test
  fun testThreeOutcomes() {
    // A FlexibleLiteralChoiceTerminal matches the SHAPE of the token leniently (so a wrong token can
    // still be located/highlighted) and flags it valid only when it equals one of the choices.
    val grammar = SequenceCombinator(FlexibleLiteralChoiceTerminal("on", "off"), EOF())

    // Consumes the whole value with every token valid -> Valid.
    assertTrue(isValid(grammar, "on"))
    assertTrue(isValid(grammar, "off"))

    // Well-formed shape, but not one of the choices -> SemanticError pointing at the bad token.
    val semantic = grammar.validate("bad")
    assertTrue(semantic is ParseOutcome.SemanticError)
    assertEquals("bad", (semantic as ParseOutcome.SemanticError).badToken.text)

    // Doesn't even match the shape at offset 0 -> SyntaxError, stuck at the start.
    val syntax = grammar.validate("123") as? ParseOutcome.SyntaxError
    assertNotNull(syntax)
    assertEquals(0, syntax!!.furthest)
    assertTrue(syntax.expected.any { it is FlexibleLiteralChoiceTerminal })
  }

  @Test
  fun testSyntaxErrorLocalizesWhereItGetsStuck() {
    // Trailing junk after a complete match: the value is consumed up to offset 2, then EOF can't
    // match the rest. Returning Stuck as a value is what lets us report furthest = 2 and that EOF
    // (end-of-input) was expected there.
    val grammar = SequenceCombinator(FlexibleLiteralChoiceTerminal("on", "off"), EOF())

    val syntax = grammar.validate("on off") as? ParseOutcome.SyntaxError
    assertNotNull(syntax)
    assertEquals(2, syntax!!.furthest)
    assertTrue(syntax.expected.any { it is EOF })
  }

  @Test
  fun testExpectedSetSeedsCompletionAtStart() {
    // The frontier's "expected set" is exactly what completion needs: at the caret position, which
    // tokens could legally come next? For the empty value at offset 0, the grammar expects one of
    // its leading terminals.
    val grammar = SequenceCombinator(
      AlternativeCombinator(LiteralChoiceTerminal("none"), FlexibleLiteralChoiceTerminal("on", "off")),
      EOF()
    )
    val outcome = grammar.validate("") as? ParseOutcome.SyntaxError
    assertNotNull(outcome)
    assertEquals(0, outcome!!.furthest)
    assertTrue(outcome.expected.any { it is LiteralChoiceTerminal })          // "none"
    assertTrue(outcome.expected.any { it is FlexibleLiteralChoiceTerminal })  // "on" / "off"
  }

  @Test
  fun testIntegerRangeGrammar() {
    // Equivalent to the config_parse_ip_port grammar: a port in [0, 65536).
    val grammar = SequenceCombinator(IntegerTerminal(0, 65536), EOF())
    assertTrue(isValid(grammar, "0"))
    assertTrue(isValid(grammar, "65535"))
    assertTrue(!isValid(grammar, "65536")) // out of range -> well-formed but invalid
    assertTrue(!isValid(grammar, "-1"))
    assertTrue(!isValid(grammar, "80x"))

    assertTrue(grammar.validate("65536") is ParseOutcome.SemanticError) // int matched, range failed
  }

  @Test
  fun testGreedyCaseTheOldEngineFails() {
    // Built from the SAME combinator classes the old engine uses. Seq(ZeroOrMore("a"), "a") on "aa"
    // fails under SyntacticMatch/SemanticMatch (the star eats both a's) but succeeds under parse().
    val grammar = SequenceCombinator(ZeroOrMore(LiteralChoiceTerminal("a")), LiteralChoiceTerminal("a"), EOF())

    assertTrue(isValid(grammar, "a"))
    assertTrue(isValid(grammar, "aa"))
    assertTrue(isValid(grammar, "aaa"))
    assertTrue(!isValid(grammar, ""))   // needs at least one "a"
    assertTrue(!isValid(grammar, "ab")) // trailing junk

    // Demonstrate the old engine really does fail "aa" (documents the difference, not just asserts ours).
    val oldEngineFullMatch = grammar.SemanticMatch("aa", 0).matchResult
    assertEquals(-1, oldEngineFullMatch)
  }

  @Test
  fun testSemanticErrorPicksTheParseThatStayedValidLongest() {
    // Ambiguous, invalid value: "ab" parses two ways, each full, each with a different bad token.
    //   - via the first branch:  [a]=valid, [b]=invalid   -> first bad at offset 1
    //   - via the second branch: [ab]=invalid             -> first bad at offset 0
    // validate() must report the bad token from the parse that stayed valid the longest (offset 1),
    // NOT whichever the lazy stream yields first. We prove it's order-invariant by declaring the two
    // alternatives in both orders and getting the same answer.
    val aThenBadB = SequenceCombinator(LiteralChoiceTerminal("a"), FlexibleLiteralChoiceTerminal("x"), EOF())
    val badAB = SequenceCombinator(FlexibleLiteralChoiceTerminal("zz"), EOF())

    for (grammar in listOf(AlternativeCombinator(aThenBadB, badAB), AlternativeCombinator(badAB, aThenBadB))) {
      val outcome = grammar.validate("ab")
      assertTrue(outcome is ParseOutcome.SemanticError)
      val bad = (outcome as ParseOutcome.SemanticError).badToken
      assertEquals("b", bad.text)
      assertEquals(1, bad.start)
    }
  }

  @Test
  fun testSemanticErrorTieBreaksOnDeclarationOrder() {
    // Two enums over the same character shape: "baz" matches the shape of both but equals neither, so
    // both full parses put their bad token at the SAME offset (0). The tie is broken by stream order
    // = the earlier-declared alternative, so the reported token's terminal (and thus its quick-fix
    // choices) is the first one. This pins the behaviour an author can steer by ordering.
    val foo = SequenceCombinator(FlexibleLiteralChoiceTerminal("foo"), EOF())
    val bar = SequenceCombinator(FlexibleLiteralChoiceTerminal("bar"), EOF())

    val fooFirst = (AlternativeCombinator(foo, bar).validate("baz") as ParseOutcome.SemanticError).badToken
    assertEquals("baz", fooFirst.text)
    assertTrue((fooFirst.terminal as FlexibleLiteralChoiceTerminal).choices.contains("foo"))

    val barFirst = (AlternativeCombinator(bar, foo).validate("baz") as ParseOutcome.SemanticError).badToken
    assertTrue((barFirst.terminal as FlexibleLiteralChoiceTerminal).choices.contains("bar"))
  }
}
