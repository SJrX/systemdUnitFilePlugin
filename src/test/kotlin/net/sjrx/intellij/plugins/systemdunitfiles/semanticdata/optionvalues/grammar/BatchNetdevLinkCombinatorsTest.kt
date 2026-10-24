package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Grammar-level tests for the shared helpers in BatchNetdevLinkCombinators.kt, run against BOTH matching
 * engines: the classic SyntacticMatch/SemanticMatch pair and the list-of-successes [validate]. The cases
 * come from systemd's own unit tests at 0f0682b773 (test-parse-util.c, test-time-util.c,
 * test-cpu-set-util.c) where one exists, otherwise from sd-check. The integer and route-table terminals
 * themselves are covered by SafeAtoTerminalTest and RouteTableTest.
 */
class BatchNetdevLinkCombinatorsTest {

  private fun classicAccepts(grammar: Combinator, value: String): Boolean =
    grammar.SyntacticMatch(value, 0).matchResult == value.length && grammar.SemanticMatch(value, 0).matchResult == value.length

  private fun assertAccepts(grammar: Combinator, expected: Boolean, vararg values: String) {
    val whole = SequenceCombinator(grammar, EOF())
    for (value in values) {
      assertEquals("classic engine on '$value'", expected, classicAccepts(whole, value))
      assertEquals("parse engine on '$value'", expected, whole.validate(value) == ParseOutcome.Valid)
    }
  }

  @Test
  fun testSiSizeMatchesParseSizeSiTests() {
    // TEST(parse_size_si) successes.
    assertAccepts(siSize(), true, "111", "111.4", "112 B", "112.6 B", "3.5 K", "3. K", "3.0 K", "4 M 11.5K",
      "3.5G3B", "3.5G 4B", "4T3G3B", "4 T 3 G 3 B", "12P", "3E 2P", "1000E", "10.M", "+5K")
    // TEST(parse_size_si) failures (bar 1000E, an overflow this grammar does not compute).
    assertAccepts(siSize(), false, "3. 0 K", "3B3.5G", "3B3G4T", "12P12P", "12X", "12.5X", "12.5e3", "-1", "-1000E",
      "-10B 20K", "1k", "1 2")
  }

  @Test
  fun testNsecDurationMatchesParseNsecTests() {
    // TEST(parse_nsec) successes, minus the two "infinity" spellings this grammar deliberately excludes.
    assertAccepts(NSEC_DURATION_FINITE, true, "5s", "5s500ms", "5s 500ms", "5.5s", "5.5s 0.5ms", ".22s", ".50y",
      "2.5", ".7", "+3.1s", "3.1s.2", "3.1 .2s", "3.1 sec .2 sec", "3.1 sec 1.2 sec", "1.111111111111y", "100 ns", "1µs")
    assertAccepts(NSEC_DURATION_FINITE, false, "xyz", ".", "5.", ".s", "-5s", "-0.3s", "0.-0s", "3.-0s", "3.+1s",
      "3. 1s", "3.s", "12..34", "..1234", "1234..", "infinity")
  }

  @Test
  fun testCpuSetMatchesParseCpuSetTests() {
    // TEST(parse_cpu_set) successes.
    assertAccepts(CPU_SET, true, "0", "1 2 4", "0 1 2 3 8 9 10 11", "8 '9' 10 \"11\"", "0,1,2,3 8,9,10,11",
      "0, 1, 2, 3, 4, 5, 6, 7, 63, ", "0-3,8-11", "36-39,44-47", "64-71", "0-3  8-11, ", "0-7 4-11", "0,2 4-11",
      "8000-8191", "0x0-0x3", ",0")
    // TEST(parse_cpu_set) failures (bar "3-0", a reversed range this grammar does not compare).
    assertAccepts(CPU_SET, false, "0 1 2 3 garbage", "0-3 8-garbage", "0 1 2 3 \"4 5 6 7 ", "8192", "1-", "-1")
    // EXTRACT_UNQUOTE removes quotes anywhere in a word, and without EXTRACT_CUNESCAPE a backslash just
    // makes the next character literal.
    assertAccepts(CPU_SET, true, "0-\"3\"", "'0'-'3'", "\"0\"-3, 4", "0-\\3", "\" 3\"", "1-\"0x3\"")
    // The lower bound of a range is cut at the first `-`, so `-0` is an empty word there.
    assertAccepts(CPU_SET, false, "-0", "-0-3", "0-\"9000\"", "\"0-3", "\"3 \"", "0-\"\"", "0-", "0- 3")

    // The bounds are SafeAtoTerminal numbers in a word split on whitespace and commas (sd-check, IRQAffinity=):
    // a sign after 0b/0o is fine, but `0b 1` is two words and the lower bound of `0b-0` is the bare word `0b`.
    assertAccepts(CPU_SET, true, "0b+1", "0-0b+1", "0-0b-0", "0o17", "\"0b 1\"")
    assertAccepts(CPU_SET, false, "0b-0", "0b 1", "0-0b 1", "0b-1-3")
  }

  @Test
  fun testParseRangeAcceptsSplitsAtTheFirstDash() {
    val lower = SafeAtoTerminal(
      signed = false,
      base = 0,
      flags = emptySet(),
      minInclusive = 0L,
      maxInclusive = 99L,
      acceptsWhitespace = true,
      acceptsMinus = false,
    )
    val upper = SafeAtoTerminal(
      signed = false,
      base = 0,
      flags = emptySet(),
      minInclusive = 0L,
      maxInclusive = 99L,
      acceptsWhitespace = true,
      acceptsMinus = true,
    )
    for (word in listOf("5", "5-7", "5--0", "0x5-+7", "0", " 5", "5- 7", "0b 1-0b+1")) {
      assertEquals("parse_range accepts <$word>", true, netdevLinkParseRangeAccepts(word, lower, upper))
    }
    for (word in listOf("", "-0", "-5", "5-", "5 -7", "100", "5-100", "5-0b-1", "0b-0")) {
      assertEquals("parse_range refuses <$word>", false, netdevLinkParseRangeAccepts(word, lower, upper))
    }
  }

  @Test
  fun testExtractWordUnquotesAndUnescapes() {
    assertEquals("FOO=bar baz", extractWord("\"FOO=bar baz\"", true))
    assertEquals("FOO=bar", extractWord("F\"OO\"=bar", true))
    assertEquals("FOOA=1", extractWord("FOO\\x41=1", true))
    assertEquals("a b", extractWord("a\\sb", true))
    assertEquals("a\u00e9", extractWord("a\\u00e9", true))
    assertEquals("a\u0001", extractWord("a\\001", true))
    assertEquals(null, extractWord("FOO\\q", true))
    assertEquals(null, extractWord("a\\x00", true))
    assertEquals(null, extractWord("a\\400", true))
    assertEquals(null, extractWord("a\\uD800", true))
    assertEquals(null, extractWord("\"abc", true))
    assertEquals(null, extractWord("abc\\", false))
    assertEquals("aq", extractWord("a\\q", false))
  }

  @Test
  fun testKeyFilePathRefusesApiVfsAndDotDot() {
    assertAccepts(NON_API_VFS_ABSOLUTE_PATH, true, "/etc/key", "/etc/my key", "/devices/x", "/sysroot/key", "/etc//a/./b/",
      "/./devices/x", "/.dev/x", "/..dev/x")
    assertAccepts(NON_API_VFS_ABSOLUTE_PATH, false, "key", "/dev", "/dev/null", "//sys/x", "/proc/1/environ", "/a/../b", "/a/..",
      "/./dev/shm/key", "/.//./proc", "//./sys/", "/dev/")
  }

  @Test
  fun testWireguardKeyNeedsPaddingAndAValidLastCharacter() {
    assertAccepts(WIREGUARD_BASE64_KEY, true, "EEGlnEPYJV//kbvvIqxKkQwOiS+UENyPncC4bF46ong=",
      "EEGlnEPYJV__kbvvIqxKkQwOiS-UENyPncC4bF46ong=", "EEGlnEPYJV//kbvvIqxK kQwOiS+UENyPncC4bF46ong =")
    assertAccepts(WIREGUARD_BASE64_KEY, false, "EEGlnEPYJV//kbvvIqxKkQwOiS+UENyPncC4bF46ong",
      "EEGlnEPYJV//kbvvIqxKkQwOiS+UENyPncC4bF46onh=", "EEGlnEPYJV//kbvvIqxKkQwOiS+UENyPncC4bF46ong==", "AAAA")
  }

  @Test
  fun testSafePrefixLengthIsASafeAtou8WithinTheFamilyWidth() {
    // sd-check (AllowedIPs=): `/0b+1000`, `/0o10` and `0.0.0.0/0b-0` are accepted; `/0b-1` is refused, and so are
    // `/0b 1` and `/ 8`, which the list splits into two words.
    assertAccepts(IP_ADDR_AND_SAFE_PREFIX, true, "10.0.0.0/8", "10.0.0.0/0x8", "10.0.0.0/0b+1000", "10.0.0.0/0o10",
      "0.0.0.0/0b-0", "10.0.0.1", "fd00::/0x40", "::/128")
    assertAccepts(IP_ADDR_AND_SAFE_PREFIX, false, "10.0.0.0/33", "10.0.0.0/0b-1", "10.0.0.0/0b 1", "10.0.0.0/ 8",
      "fd00::/129", "10.0.0.0/")
  }

  @Test
  fun testCheckedSpanRejectsWhatItsCheckRefusesOnBothEngines() {
    val multicast = NetdevLinkCheckedSpan(IP_ADDR, "multicast") { netdevLinkIsMulticast(it) }
    assertAccepts(multicast, true, "224.0.0.1", "239.255.255.255", "ff02::1", "FF05::2")
    assertAccepts(multicast, false, "223.255.255.255", "240.0.0.1", "ff::1", "::ffff:239.1.1.1", "10.0.0.1", "239.1.1")
  }

  @Test
  fun testParseSizeSiTotals() {
    assertEquals(java.math.BigInteger("12000000000000000"), netdevLinkParseSizeSi("12P"))
    assertEquals(java.math.BigInteger("3002000000000000000"), netdevLinkParseSizeSi("3E 2P"))
    assertEquals(java.math.BigInteger("4011500"), netdevLinkParseSizeSi("4 M 11.5K"))
    assertEquals(java.math.BigInteger("10000000"), netdevLinkParseSizeSi("10.M"))
    assertEquals(java.math.BigInteger("111"), netdevLinkParseSizeSi("111.4"))
    assertEquals(java.math.BigInteger("5000"), netdevLinkParseSizeSi("+5K"))
    assertEquals(null, netdevLinkParseSizeSi("5k"))
  }
}
