package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseAddressFamiliesOptionValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the list-of-successes matcher `Combinator.parse()` (#467 step 2).
 *
 * The point of these tests is that parse()/validate() run against the EXISTING combinator classes
 * and the REAL production grammars — nothing in the 200+ grammar definitions changed. We just grew
 * a second matching method on the same combinators.
 */
class ParseTest {

  private fun isValid(grammar: Combinator, value: String) = grammar.validate(value) == ParseOutcome.Valid

  @Test
  fun testRealAddressFamiliesGrammarValidates() {
    // The actual production grammar, unchanged — pulled straight off the validator.
    val grammar = ConfigParseAddressFamiliesOptionValue().combinator

    val valid = listOf(
      "none", "AF_INET", "AF_INET AF_INET6", "AF_UNIX AF_NETLINK", "~AF_PACKET",
      "~AF_INET AF_INET6", "AF_BRIDGE AF_X25 AF_AX25", "AF_LOCAL", "AF_DECnet",
      "AF_VSOCK AF_XDP AF_MCTP", "~AF_UNIX AF_INET AF_INET6 AF_NETLINK AF_PACKET",
    )
    val invalid = listOf(
      "inet", "AF_inet", "AF_INET, AF_INET6", "~ AF_PACKET", "NONE",
      "AF_BOGUS", "AF_INETZ", "AF_INET AF_MADEUP", "AF_DECNET",
    )
    for (v in valid) assertTrue("expected valid: '$v'", isValid(grammar, v))
    for (v in invalid) assertTrue("expected invalid: '$v'", !isValid(grammar, v))
  }

  @Test
  fun testAddressFamiliesErrorKinds() {
    val grammar = ConfigParseAddressFamiliesOptionValue().combinator

    // Well-formed shape, unknown name -> semantic error pointing at the bad token.
    val semantic = grammar.validate("AF_BOGUS")
    assertTrue(semantic is ParseOutcome.SemanticError)
    assertEquals("AF_BOGUS", (semantic as ParseOutcome.SemanticError).badToken.text)

    // Comma breaks the shape after "AF_INET" -> syntax error (malformed, not just an unknown name).
    // Thanks to the frontier layer we now report WHERE it got stuck (offset 7, the comma) and WHAT
    // was expected there: another whitespace-separated family, or end-of-input.
    val syntax = grammar.validate("AF_INET, AF_INET6") as? ParseOutcome.SyntaxError
    assertNotNull(syntax)
    assertEquals(7, syntax!!.furthest)
    assertTrue(syntax.expected.any { it is WhitespaceTerminal })
    assertTrue(syntax.expected.any { it is EOF })
  }

  @Test
  fun testFrontierSeedsCompletionAtStart() {
    // The frontier's "expected set" is exactly what completion (#343) needs: at the caret position,
    // which tokens could legally come next? For the empty value at offset 0, the grammar expects
    // "none", the "~" inversion prefix, or an address-family name.
    val grammar = ConfigParseAddressFamiliesOptionValue().combinator
    val outcome = grammar.validate("") as? ParseOutcome.SyntaxError
    assertNotNull(outcome)
    assertEquals(0, outcome!!.furthest)
    assertTrue(outcome.expected.any { it is FlexibleLiteralChoiceTerminal }) // the AF_* names
    assertTrue(outcome.expected.any { it is LiteralChoiceTerminal })         // "none" / "~"
  }

  @Test
  fun testRealIpv6GrammarValidates() {
    // IPV6_ADDR is the real, hand-ordered Alt of 15+ forms in Combinators.kt. The old engine needed
    // that careful ordering to avoid greedy traps; parse() explores all forms, so it just works.
    val grammar = SequenceCombinator(IPV6_ADDR, EOF())

    val valid = listOf("::", "::1", "fe80::1", "2001:db8::1", "1:2:3:4:5:6:7:8", "::ffff:192.168.0.1")
    val invalid = listOf("2001:db8:::1", "1:2:3:4:5:6:7:8:9", "gggg::1", "")
    for (v in valid) assertTrue("expected valid IPv6: '$v'", isValid(grammar, v))
    for (v in invalid) assertTrue("expected invalid IPv6: '$v'", !isValid(grammar, v))
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
