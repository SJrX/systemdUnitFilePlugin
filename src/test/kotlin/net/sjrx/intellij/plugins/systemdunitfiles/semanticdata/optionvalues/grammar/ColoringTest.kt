package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseAddressFamiliesOptionValue
import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for grammar coloring: default roles, automatic coloring, and the optional Labeled wrapper. */
class ColoringTest {

  @Test
  fun testDefaultRoles() {
    assertEquals(Role.LITERAL, defaultRole(IntegerTerminal(0, 10)))
    assertEquals(Role.ENUM, defaultRole(LiteralChoiceTerminal("none")))
    assertEquals(Role.ENUM, defaultRole(FlexibleLiteralChoiceTerminal("AF_INET", "AF_INET6")))
    assertEquals(Role.OPERATOR, defaultRole(LiteralChoiceTerminal(":")))
    assertEquals(Role.OPERATOR, defaultRole(LiteralChoiceTerminal("~")))
    assertEquals(Role.IDENTIFIER, defaultRole(RegexTerminal("[a-z]+", "[a-z]+")))
    assertEquals(null, defaultRole(WhitespaceTerminal()))
  }

  @Test
  fun testAutomaticColoringNeedsNoGrammarChanges() {
    // The real RestrictAddressFamilies grammar, unchanged: "~" is an operator, families are enums,
    // whitespace is uncoloured.
    val grammar = ConfigParseAddressFamiliesOptionValue().combinator
    val regions = grammar.colorize("~AF_INET AF_INET6")
    assertEquals(
      listOf(
        Region(0, 1, Role.OPERATOR),  // ~
        Region(1, 8, Role.ENUM),      // AF_INET
        Region(9, 17, Role.ENUM),     // AF_INET6 (the space at 8..9 is uncoloured)
      ),
      regions,
    )
  }

  @Test
  fun testLabeledPaintsACompositeSpanAsOneUnit() {
    // Without Labeled an IPv4 address would colour per octet/dot; wrapping it makes it one LITERAL.
    val grammar = SequenceCombinator(Labeled(Role.LITERAL, IPV4_ADDR), EOF())
    assertEquals(listOf(Region(0, 7, Role.LITERAL)), grammar.colorize("1.2.3.4"))
  }

  @Test
  fun testLabeledIsTransparentToValidation() {
    // Wrapping changes only colour: validation behaves exactly as the bare grammar.
    val bare = SequenceCombinator(IPV4_ADDR, EOF())
    val labeled = SequenceCombinator(Labeled(Role.LITERAL, IPV4_ADDR), EOF())
    assertEquals(bare.validate("1.2.3.4"), labeled.validate("1.2.3.4"))
    assertEquals(ParseOutcome.Valid, labeled.validate("1.2.3.4"))
    assertEquals(bare.validate("999.0.0.1")::class, labeled.validate("999.0.0.1")::class)
  }
}
