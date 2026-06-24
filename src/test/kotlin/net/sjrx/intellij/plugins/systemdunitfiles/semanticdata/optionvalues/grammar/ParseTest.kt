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
}
