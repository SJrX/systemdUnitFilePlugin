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
    assertEquals(null, defaultRole(RegexTerminal("[a-z]+", "[a-z]+"))) // free-form: keeps default value color
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
    // Without Labeled these would colour as two ENUM tokens; wrapping makes the span one LITERAL.
    val grammar = Labeled(Role.LITERAL, SequenceCombinator(LiteralChoiceTerminal("a"), LiteralChoiceTerminal("b")))
    assertEquals(listOf(Region(0, 2, Role.LITERAL)), grammar.colorize("ab"))
  }

  @Test
  fun testSharedIpCombinatorIsLabeledAsOneLiteral() {
    // IPV4_ADDR is wrapped in Labeled in Combinators.kt, so an address colours as one literal.
    assertEquals(listOf(Region(0, 7, Role.LITERAL)), SequenceCombinator(IPV4_ADDR, EOF()).colorize("1.2.3.4"))
  }

  @Test
  fun testLabeledCarriesSemanticTag() {
    // The optional tag rides on the Region so a feature can recognise a span by what the grammar
    // declared it to be, instead of re-sniffing the text. Untagged Labeled spans stay tag == null.
    val tagged = Labeled(Role.LITERAL, LiteralChoiceTerminal("ab"), SemanticTag.IPV6)
    assertEquals(listOf(Region(0, 2, Role.LITERAL, SemanticTag.IPV6)), tagged.labeledRegions("ab"))
    assertEquals(listOf(Region(0, 2, Role.LITERAL, null)), Labeled(Role.LITERAL, LiteralChoiceTerminal("ab")).labeledRegions("ab"))
  }

  @Test
  fun testIpCombinatorsDeclareTheirIdentityStructurally() {
    // IPV6_ADDR declares itself IPv6; IPV4_ADDR is untagged. The IPv6 inspection keys off this tag,
    // so it never has to guess whether a literal span "looks like" an IPv6 address.
    assertEquals(
      listOf(Region(0, 3, Role.LITERAL, SemanticTag.IPV6)),
      SequenceCombinator(IPV6_ADDR, EOF()).labeledRegions("::1"),
    )
    assertEquals(
      listOf(Region(0, 7, Role.LITERAL, null)),
      SequenceCombinator(IPV4_ADDR, EOF()).labeledRegions("1.2.3.4"),
    )
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
