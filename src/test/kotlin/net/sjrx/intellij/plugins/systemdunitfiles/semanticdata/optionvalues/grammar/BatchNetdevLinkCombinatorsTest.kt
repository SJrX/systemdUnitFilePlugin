package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Grammar-level tests for the shared helpers in BatchNetdevLinkCombinators.kt, run against BOTH matching
 * engines: the classic SyntacticMatch/SemanticMatch pair and the list-of-successes [validate]. The cases
 * come from systemd's own unit tests at 0f0682b773 (test-parse-util.c, test-time-util.c,
 * test-cpu-set-util.c) where one exists.
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
  fun testSafeAtouAcceptsEveryBaseSystemdDoes() {
    val u16 = safeAtou(UINT16_LIMIT)
    assertAccepts(u16, true, "0", "65535", "0xffff", "0XFF", "0177777", "0b101", "0B1", "0o17", "0O17", "+5", "+0x10", "-0", "-0x0")
    assertAccepts(u16, false, "65536", "0x10000", "089", "0b2", "0o8", "-1", "-0x1", "-0b0", "+0b1", "0x", "abc", "1.5")
  }

  @Test
  fun testSafeAtouHonoursTheLowerBound() {
    assertAccepts(safeAtou(UINT32_LIMIT, 1), true, "1", "4294967295")
    assertAccepts(safeAtou(UINT32_LIMIT, 1), false, "0", "00", "0x0", "4294967296")
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
  }

  @Test
  fun testParseRangeBounds() {
    assertAccepts(parseRange(100), true, "5", "5-7", "5--0", "0x5-+7", "0")
    assertAccepts(parseRange(100), false, "-0", "-5", "5-", "5- 7", "5 -7", "100", "5-100")
    assertAccepts(parseRange(UINT16_LIMIT, 1, spaceAfterDash = true), true, "1000- 2000", "1000-2000")
    assertAccepts(parseRange(UINT16_LIMIT, 1, spaceAfterDash = true), false, "1000 -2000", "0-5", "5--0")
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
  fun testRouteTableAcceptsNamesAndNonZeroNumbers() {
    assertAccepts(ROUTE_TABLE, true, "main", "local", "default", "1", "0x10", "4294967295", "custom", "1abc", "-1")
    assertAccepts(ROUTE_TABLE, false, "0", "00", "4294967296", "a b", "name:1")
  }
}
