package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarEngineVerdict.Kind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [ROUTE_TABLE_FROM_STRING] against manager_get_route_table_from_string() at systemd 0f0682b773.
 *
 * Values in [testAcceptedBySystemd] and the rejected tests were confirmed with steve-local/systemd-tools/bin/sd-check
 * on [Route] Table=, [RoutingPolicyRule] Table=, [DHCPv4] RouteTable= and [VRF] Table=, which agree on every value.
 * sd-check loads no networkd.conf, so the values in [testAcceptedAsPossibleUserDefinedNames] are rejected there;
 * they are accepted on purpose, because RouteTable=NAME:NUMBER in networkd.conf could define them.
 */
class RouteTableTest {

  private fun assertVerdict(value: String, expected: GrammarEngineVerdict) {
    assertEquals("classic engine on <$value>", expected, GrammarEngineVerdict.classic(ROUTE_TABLE_FROM_STRING, value))
    assertEquals("parse engine on <$value>", expected, GrammarEngineVerdict.parseEngine(ROUTE_TABLE_FROM_STRING, value))
  }

  private fun accepted(vararg values: String) {
    for (value in values) assertVerdict(value, GrammarEngineVerdict.VALID)
  }

  @Test
  fun testAcceptedBySystemd() {
    accepted("default", "main", "local")
    accepted("1", "253", "254", "255", "4294967295", "010", "037777777777")
    // Not all digits, so safe_atou32 reads them, with all its spellings.
    accepted("0x10", "+5", "0b101", "0b 1")
  }

  @Test
  fun testAcceptedAsPossibleUserDefinedNames() {
    // Rejected by sd-check, which has no networkd.conf; RouteTable=Main:100 etc. would make each of them valid.
    accepted("Main", "mytable", "yes", "0x0", "-0", "-1", "0x100000000")
    // extract_first_word(…, 0) unescapes "my\ table:100" to the name "my table".
    accepted("my table", "a\tb")
  }

  @Test
  fun testAllDigitTableNumbersOutOfRange() {
    // All digits: never a name, so safe_atou32 decides, and 0 is refused with ERANGE.
    for (value in listOf("0", "00", "4294967296", "040000000000", "99999999999999999999999")) {
      assertVerdict(value, GrammarEngineVerdict(Kind.SEMANTIC, 0, value.length))
    }
  }

  @Test
  fun testMalformedAllDigitNumber() {
    // "08" is not octal: EINVAL, highlighted where strtoul stopped.
    assertVerdict("08", GrammarEngineVerdict(Kind.SYNTAX, 1, 2))
    assertVerdict("0779", GrammarEngineVerdict(Kind.SYNTAX, 3, 4))
  }

  @Test
  fun testColonIsNeverPartOfATable() {
    // A name is cut at the first ':' in networkd.conf, and safe_atou32 has no ':' either.
    assertVerdict("a:5", GrammarEngineVerdict(Kind.SYNTAX, 1, 3))
    assertVerdict("main:5", GrammarEngineVerdict(Kind.SYNTAX, 4, 6))
    assertVerdict("123:5", GrammarEngineVerdict(Kind.SYNTAX, 3, 5))
    assertVerdict("0x0:5", GrammarEngineVerdict(Kind.SYNTAX, 3, 5))
    assertVerdict(":5", GrammarEngineVerdict(Kind.SYNTAX, 0, 2))
  }

  @Test
  fun testUserDefinedNameShape() {
    // Exactly the networkd.conf rules: non-empty, no ':', not only digits.
    val name = SequenceCombinator(ROUTE_TABLE_USER_DEFINED_NAME, EOF())
    for (value in listOf("x", "main", "1a", "a1", "0x0", "-0", "my table", "é")) {
      assertEquals(value, ParseOutcome.Valid, name.validate(value))
    }
    for (value in listOf("1", "0123456789", "a:b", ":", "1:a")) {
      assertTrue(value, name.validate(value) is ParseOutcome.SyntaxError)
    }
  }

  @Test
  fun testPredefinedNamesAreOfferedForCompletion() {
    assertEquals(setOf("default", "main", "local"), ROUTE_TABLE_FROM_STRING.nextTokenChoices(""))
  }
}
