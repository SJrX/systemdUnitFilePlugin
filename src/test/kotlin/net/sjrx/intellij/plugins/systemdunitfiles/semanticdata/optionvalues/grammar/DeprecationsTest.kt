package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseAddressFamiliesOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseAddressSectionDadOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseIpMasqueradeOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseUnitConditionStringOptionValue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for valid-but-deprecated value detection on the RestrictAddressFamilies grammar. */
class DeprecationsTest {

  private val grammar = ConfigParseAddressFamiliesOptionValue().combinator

  @Test
  fun testRemovedFamilyIsReportedAtItsExactSpan() {
    val deprecated = grammar.deprecatedTokens("AF_INET AF_DECnet")
    assertEquals(1, deprecated.size)
    val it = deprecated.single()
    assertEquals(8, it.start)  // "AF_INET " == 8 chars
    assertEquals(17, it.end)   // + "AF_DECnet"
    assertTrue(it.message.contains("AF_DECnet"))
    assertTrue(it.message.contains("removed"))
  }

  @Test
  fun testCurrentFamiliesAreNotReported() {
    assertTrue(grammar.deprecatedTokens("AF_INET AF_INET6 AF_UNIX").isEmpty())
  }

  @Test
  fun testInvalidValueReportsNoDeprecations() {
    // No full parse -> nothing (the InvalidValue inspection handles the error instead).
    assertTrue(grammar.deprecatedTokens("AF_DECnet AF_BOGUS").isEmpty())
  }

  @Test
  fun testIpMasqueradeBooleanValuesAreDeprecated() {
    // A second, unrelated user of the same layer: the legacy boolean forms of IPMasquerade= are
    // accepted but deprecated in favour of ipv4/ipv6/both/no.
    val ipMasquerade = ConfigParseIpMasqueradeOptionValue().combinator
    assertTrue(ipMasquerade.deprecatedTokens("true").single().message.contains("ipv4"))
    assertTrue(ipMasquerade.deprecatedTokens("off").single().message.contains("no"))
    assertTrue(ipMasquerade.deprecatedTokens("ipv4").isEmpty())
    assertTrue(ipMasquerade.deprecatedTokens("both").isEmpty())
  }

  @Test
  fun testDuplicateAddressDetectionBooleansAreDeprecatedButAccepted() {
    // config_parse_address_dad tries parse_boolean() first and accepts the result with a
    // "For historical reasons" warning, so these are valid values that deserve a nudge, not errors.
    val dad = ConfigParseAddressSectionDadOptionValue().combinator
    for (spelling in listOf("yes", "no", "1", "0", "off")) {
      val deprecated = dad.deprecatedTokens(spelling)
      assertEquals(spelling, 1, deprecated.size)
      assertTrue(spelling, deprecated.single().message.contains("historical reasons"))
    }
    // The four family names are the spelling systemd asks for, so they carry no note...
    for (name in listOf("none", "both", "ipv4", "ipv6")) {
      assertTrue(name, dad.deprecatedTokens(name).isEmpty())
    }
  }

  @Test
  fun testDeprecatingOneBooleanTerminalDoesNotPoisonTheSharedOne() {
    // deprecatedBoolean() has to hand back a FRESH terminal: FlexibleLiteralChoiceTerminal.deprecating
    // mutates in place, so reusing the shared BOOLEAN would attach DuplicateAddressDetection='s note to
    // every boolean-valued setting in the plugin.
    assertTrue(BOOLEAN.deprecationFor("yes") == null)
    val firstBoot = ConfigParseUnitConditionStringOptionValue().combinator
    assertTrue(firstBoot.deprecatedTokens("yes").isEmpty())
    assertTrue(firstBoot.deprecatedTokens("|! no").isEmpty())
    // ...and the two really are separate instances, not the same object reached twice.
    assertFalse(deprecatedBoolean("a") === deprecatedBoolean("b"))
  }
}
