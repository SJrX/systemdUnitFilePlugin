package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseEtherAddrsOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseHwAddrOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseHwAddrsOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseSrIovMacOptionValue
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Grammar-level tests (new list-of-successes engine, #501) for the MAC / hardware-address validators.
 * Exercises the two syntaxes and — the point the man pages hide — that a hardware-address field
 * (Match=, Link=) also accepts IPv4/IPv6 literals, while a strict 6-byte MAC field (SR-IOV=) does not.
 */
class MacAddressGrammarTest {

  private fun Combinator.accepts(value: String) = validate(value) == ParseOutcome.Valid

  // A single 6-byte Ethernet MAC (config_parse_ether_addr): SR-IOV=, BridgeFDB=, MACsec=, ...
  private val mac = ConfigParseSrIovMacOptionValue().combinator
  // A single hardware address, expected_len == 0 (config_parse_hw_addr): Link=, Neighbor=.
  private val hwAddr = ConfigParseHwAddrOptionValue().combinator
  // Whitespace-separated hardware-address list (config_parse_hw_addrs): Match=, PermanentMACAddress=.
  private val hwAddrs = ConfigParseHwAddrsOptionValue().combinator
  // Whitespace-separated 6-byte MAC list (config_parse_ether_addrs): MACVLAN/MACVTAP SourceMACAddress=.
  private val macs = ConfigParseEtherAddrsOptionValue().combinator

  @Test
  fun testSixByteMacAcceptsAllThreeSeparatorStyles() {
    assertEquals(true, mac.accepts("00:11:22:33:44:55"))  // colon, 1-byte fields
    assertEquals(true, mac.accepts("00-11-22-33-44-55"))  // hyphen, 1-byte fields
    assertEquals(true, mac.accepts("0011.2233.4455"))     // dot, 2-byte fields
    assertEquals(true, mac.accepts("1:2:3:4:5:6"))        // single-hex-digit fields are allowed
    assertEquals(true, mac.accepts("DE:AD:BE:EF:00:01"))  // uppercase hex
  }

  @Test
  fun testSixByteMacRejectsWrongLengthAndIpLiterals() {
    assertEquals(false, mac.accepts("00:11:22:33"))           // 4 groups, not 6
    assertEquals(false, mac.accepts("00:11:22:33:44"))        // 5 groups
    assertEquals(false, mac.accepts("00:11:22:33:44:55:66"))  // 7 groups
    assertEquals(false, mac.accepts("gg:hh:ii:jj:kk:ll"))     // non-hex
    assertEquals(false, mac.accepts("192.168.1.1"))           // IPv4 literal is NOT a 6-byte MAC
    assertEquals(false, mac.accepts("2001:db8::1"))           // nor is an IPv6 literal
  }

  @Test
  fun testHardwareAddressAcceptsMacsInfinibandAndIpLiterals() {
    assertEquals(true, hwAddr.accepts("00:11:22:33:44:55"))                 // 6-byte MAC
    assertEquals(true, hwAddr.accepts("0011.2233.4455"))                    // 6-byte, dot form
    assertEquals(true, hwAddr.accepts("01:02:03:04"))                       // 4-byte hw addr
    assertEquals(true, hwAddr.accepts(List(20) { "aa" }.joinToString(":"))) // 20-byte (Infiniband)
    assertEquals(true, hwAddr.accepts("192.168.1.1"))                       // IPv4 literal
    assertEquals(true, hwAddr.accepts("2001:db8::1"))                       // IPv6 literal
    assertEquals(true, hwAddr.accepts("::1"))                               // IPv6 literal, compressed
  }

  @Test
  fun testHardwareAddressRejectsInvalidLengthsAndGarbage() {
    assertEquals(false, hwAddr.accepts("00:11:22:33:44"))       // 5 bytes: not 4/6/16/20
    assertEquals(false, hwAddr.accepts("00:11:22"))             // 3 bytes
    assertEquals(false, hwAddr.accepts("192.168.1.256"))        // octet out of range, and not a hw addr
    assertEquals(false, hwAddr.accepts("hello"))
  }

  @Test
  fun testHardwareAddressListMixesFormsAndIpLiterals() {
    assertEquals(true, hwAddrs.accepts("00:11:22:33:44:55"))
    assertEquals(true, hwAddrs.accepts("00:11:22:33:44:55 aa-bb-cc-dd-ee-ff 192.168.1.1 2001:db8::1"))
    assertEquals(false, hwAddrs.accepts("00:11:22:33:44:55 not-a-mac"))
  }

  @Test
  fun testSixByteMacListRejectsIpLiterals() {
    assertEquals(true, macs.accepts("00:11:22:33:44:55 aa:bb:cc:dd:ee:ff"))
    assertEquals(false, macs.accepts("00:11:22:33:44:55 192.168.1.1")) // list of MACs, no IP literals
  }

  @Test
  fun testAddressColorsAsOneLiteralSpan() {
    // Both combinators are wrapped in Labeled(Role.LITERAL): the whole address is a single literal
    // span (not coloured per octet / with the separators as operators).
    assertEquals(listOf(Region(0, 17, Role.LITERAL)), mac.colorize("00:11:22:33:44:55"))
    assertEquals(listOf(Region(0, 17, Role.LITERAL)), hwAddr.colorize("00-11-22-33-44-55"))
  }
}