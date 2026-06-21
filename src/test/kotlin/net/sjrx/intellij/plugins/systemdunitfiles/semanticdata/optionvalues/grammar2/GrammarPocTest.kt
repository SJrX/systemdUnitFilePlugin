package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar2

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Behavioural tests for the grammar engine PoC (#467 step 2).
 *
 * The RestrictAddressFamilies= cases mirror the canary suite
 * (ConfigParseAddressFamiliesOptionValueTest) so we can see the new engine reproduce the existing
 * behaviour before wiring it into IntelliJ.
 */
class GrammarPocTest {

  // A representative slice of the AF_* set (enough for the canary cases).
  private val families = setOf(
    "AF_INET", "AF_INET6", "AF_UNIX", "AF_NETLINK", "AF_PACKET", "AF_BRIDGE",
    "AF_X25", "AF_AX25", "AF_LOCAL", "AF_DECnet", "AF_VSOCK", "AF_XDP", "AF_MCTP",
  )

  // none | [~] family (ws family)*     — note: no EOF here; validate() requires full consumption.
  private val family = FlexibleChoice(families, role = Role.ADDRESS_FAMILY)
  private val restrictAddressFamilies: Matcher = Alt(
    Lit("none"),
    Seq(ZeroOrOne(Lit("~")), family, ZeroOrMore(Seq(Whitespace, family))),
  )

  private fun isValid(value: String) = validate(restrictAddressFamilies, value) == Outcome.Valid

  @Test
  fun testValidValues() {
    val valid = listOf(
      "none",
      "AF_INET",
      "AF_INET AF_INET6",
      "AF_UNIX AF_NETLINK",
      "~AF_PACKET",
      "~AF_INET AF_INET6",
      "AF_BRIDGE AF_X25 AF_AX25",
      // newer additions / aliases / mixed case
      "AF_LOCAL",
      "AF_DECnet",
      "AF_VSOCK AF_XDP AF_MCTP",
      "~AF_UNIX AF_INET AF_INET6 AF_NETLINK AF_PACKET",
    )
    for (v in valid) assertTrue("expected valid: '$v'", isValid(v))
  }

  @Test
  fun testInvalidValues() {
    val invalid = listOf(
      "inet", "AF_inet", "AF_INET, AF_INET6", "~ AF_PACKET", "NONE",
      "AF_BOGUS", "AF_INETZ", "AF_INET AF_MADEUP", "AF_DECNET",
    )
    for (v in invalid) assertTrue("expected invalid: '$v'", !isValid(v))
  }

  @Test
  fun testWellFormedButUnknownIsSemanticError() {
    // AF_BOGUS matches the token shape (so we can point at it) but is not a real family.
    val outcome = validate(restrictAddressFamilies, "AF_BOGUS")
    assertTrue(outcome is Outcome.SemanticError)
    val bad = (outcome as Outcome.SemanticError).badToken
    assertEquals("AF_BOGUS", bad.text)
    assertEquals(Role.ADDRESS_FAMILY, bad.role)
  }

  @Test
  fun testMalformedIsSyntaxErrorLocatedAtFurthestProgress() {
    // After "AF_INET" the comma is neither whitespace nor a family, so the shape breaks at offset 7.
    val outcome = validate(restrictAddressFamilies, "AF_INET, AF_INET6")
    assertTrue(outcome is Outcome.SyntaxError)
    assertEquals(7, (outcome as Outcome.SyntaxError).furthest)
  }

  @Test
  fun testRolesAreAvailableForColoring() {
    // The leaves of a successful parse already carry the roles a colorizer / annotator would use.
    val parse = restrictAddressFamilies.parse("~AF_INET AF_INET6", 0).first { it.end == 17 }
    val roles = parse.leaves().map { it.role }
    assertEquals(listOf(Role.KEYWORD, Role.ADDRESS_FAMILY, Role.WHITESPACE, Role.ADDRESS_FAMILY), roles)
  }

  @Test
  fun testGreedyCaseTheOldEngineFails() {
    // Seq(ZeroOrMore("a"), "a") on "aa": the old single-path greedy engine fails this because the
    // star eats both a's. List-of-successes offers the shorter star match, so the trailing "a" fits.
    val grammar = Seq(ZeroOrMore(Lit("a")), Lit("a"))
    assertTrue(validate(grammar, "a") == Outcome.Valid)
    assertTrue(validate(grammar, "aa") == Outcome.Valid)
    assertTrue(validate(grammar, "aaa") == Outcome.Valid)
    assertTrue(validate(grammar, "") != Outcome.Valid)   // needs at least one "a"
    assertTrue(validate(grammar, "ab") != Outcome.Valid) // trailing junk
  }

  @Test
  fun testLabeledProducesOneBranchSpanningItsChildren() {
    // The mechanism behind "127.0.0.1 is ONE labeled span, not blue-octet/black-dot/...":
    // Labeled collapses everything its inner matcher produced into a single Branch with a role.
    val grammar = Labeled(Role.ADDRESS_FAMILY, Seq(Lit("x", null), Lit("y", null)))
    val full = grammar.parse("xy", 0).single { it.end == 2 }

    val branch = full.nodes.single() as Branch
    assertEquals(Role.ADDRESS_FAMILY, branch.role)
    assertEquals(0, branch.start)
    assertEquals(2, branch.end)
    assertEquals(2, branch.children.size)
    assertEquals(2, full.leaves().size) // and leaves() still flattens the tree
  }
}
