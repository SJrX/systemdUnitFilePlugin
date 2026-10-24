package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseBridgeVlanIdOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseDhcp6SendOptionOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseDhcpOptionTlvOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseDhcpPdSubnetIdOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseDhcpRequestOptionsOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseDhcpServerAddressOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseDuidTypeOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseFdbInterfaceOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseLinkGroupOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseUplinkOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Combinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarEngineVerdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Verdicts of the network-dhcp-misc validators for values that are awkward to write in a unit-file fixture
 * (a vertical tab, a no-break space) or whose verdict changed when the batch's own integer terminal was replaced by
 * the shared SafeAtoTerminal (#563). Checked on both grammar engines at once.
 *
 * Every value here was run through systemd's real parsers at 0f0682b773 (steve-local/systemd-tools/bin/sd-check,
 * one value per .network file).
 */
class NetworkDhcpMiscSafeAtoVerdictTest {

  private val vt = "\u000B"
  private val nbsp = " "

  private fun accepted(grammar: Combinator, vararg values: String) {
    for (value in values) {
      assertEquals("classic engine on <$value>", GrammarEngineVerdict.VALID, GrammarEngineVerdict.classic(grammar, value))
      assertEquals("parse engine on <$value>", GrammarEngineVerdict.VALID, GrammarEngineVerdict.parseEngine(grammar, value))
    }
  }

  private fun rejected(grammar: Combinator, vararg values: String) {
    for (value in values) {
      assertNotEquals("classic engine on <$value>", GrammarEngineVerdict.VALID, GrammarEngineVerdict.classic(grammar, value))
      assertNotEquals("parse engine on <$value>", GrammarEngineVerdict.VALID, GrammarEngineVerdict.parseEngine(grammar, value))
    }
  }

  @Test
  fun testWholeValueIntegersAcceptLeadingVerticalTab() {
    // strtoul skips C isspace(), which includes \v; config_parse only strips " \t\n\r".
    accepted(ConfigParseBridgeVlanIdOptionValue().combinator, "${vt}5", "0b${vt}1")
    accepted(ConfigParseLinkGroupOptionValue().combinator, "${vt}5", "0b${vt}1")
    accepted(ConfigParseDhcpPdSubnetIdOptionValue().combinator, "${vt}5", "-0", "0b1")
    accepted(ConfigParseDuidTypeOptionValue().combinator, "${vt}5", "0b 1:2000-01-01")
  }

  @Test
  fun testSignedGroupRefusesNegativeAfterPrefix() {
    rejected(ConfigParseLinkGroupOptionValue().combinator, "0b -1", "0b-1")
  }

  @Test
  fun testUnsignedWrapAround() {
    // strtoul negates modulo 2^64 when the '-' is not the first character, so this is 1 (DUID-LLT).
    accepted(ConfigParseDuidTypeOptionValue().combinator, "0b -" + "1".repeat(64))
  }

  @Test
  fun testFieldsAfterColonOrSlashTakeCWhitespaceOnly() {
    accepted(ConfigParseDhcpServerAddressOptionValue().combinator, "192.168.5.1/${vt}24", "192.168.5.1/ 24", "192.168.5.1/0b 11000")
    rejected(ConfigParseDhcpServerAddressOptionValue().combinator, "192.168.5.1/${nbsp}24")

    accepted(ConfigParseDhcpOptionTlvOptionValue().combinator, "12:uint8:${vt}1", "${vt}12:uint8:1", "0b 1100:uint8:1", "12:uint16: 0b 1")
    rejected(ConfigParseDhcpOptionTlvOptionValue().combinator, "${nbsp}12:uint8:1", "12:uint8:${nbsp}1")

    accepted(ConfigParseDhcp6SendOptionOptionValue().combinator, "${vt}12:uint8:1", "${vt}1:12:uint8:1")
    rejected(ConfigParseDhcp6SendOptionOptionValue().combinator, "${nbsp}12:uint8:1", "${nbsp}1:12:uint8:1")

    rejected(ConfigParseDuidTypeOptionValue().combinator, "${nbsp}1")
  }

  @Test
  fun testRequestOptionsSplitsOnWhitespaceBeforeParsing() {
    // Each word reaches safe_atou32 alone, so "0b 1" is the invalid word "0b" followed by "1".
    rejected(ConfigParseDhcpRequestOptionsOptionValue().combinator, "0b 1", "0o 7", "0b +1", "0b -0", "0b-0")
    accepted(ConfigParseDhcpRequestOptionsOptionValue().combinator, "0b+1", "1 0b1 0x10 010")
  }

  @Test
  fun testInterfaceIndexIsTheWholeValue() {
    // parse_ifindex() is safe_atoi on the whole value, whitespace included.
    for (grammar in listOf(ConfigParseFdbInterfaceOptionValue().combinator, ConfigParseUplinkOptionValue().combinator)) {
      accepted(grammar, "0b 1", "${vt}5", "0o 7", "0x10", "+5", "0x" + "0".repeat(130) + "1")
      rejected(grammar, "eth 0", "0b 0", "0b -1", "eth${nbsp}0")
    }
  }
}
