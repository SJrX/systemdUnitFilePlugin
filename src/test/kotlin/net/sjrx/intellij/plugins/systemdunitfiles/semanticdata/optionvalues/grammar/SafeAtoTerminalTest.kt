package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarEngineVerdict.Kind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigInteger

/**
 * [SafeAtoTerminal] against the verdicts of systemd's real parsers at 0f0682b773.
 *
 * Every value in the whole-value tests was run through steve-local/systemd-tools/bin/sd-check, one value per file,
 * on the key named above its grammar. (The list and range tests near the end model a caller's own splitting and
 * are not sd-check probes.) "Malformed" values are the ones systemd rejects with EINVAL ("Invalid argument") and must be
 * syntax errors; "out of range" values are the ones it rejects with ERANGE (or a caller's own range check) and
 * must be semantic errors on the number token. Each value is checked under both grammar engines, including the
 * range the inspection would highlight.
 */
class SafeAtoTerminalTest {

  private fun wholeValue(terminal: SafeAtoTerminal): Combinator = SequenceCombinator(terminal, EOF())

  private fun assertVerdict(grammar: Combinator, value: String, expected: GrammarEngineVerdict) {
    assertEquals("classic engine on <$value>", expected, GrammarEngineVerdict.classic(grammar, value))
    assertEquals("parse engine on <$value>", expected, GrammarEngineVerdict.parseEngine(grammar, value))
  }

  private fun accepted(grammar: Combinator, vararg values: String) {
    for (value in values) assertVerdict(grammar, value, GrammarEngineVerdict.VALID)
  }

  /** A syntax error highlighted from [from] to the end; [from] 0 means the whole value. */
  private fun malformed(grammar: Combinator, value: String, from: Int) {
    assertVerdict(grammar, value, GrammarEngineVerdict(Kind.SYNTAX, from, value.length))
  }

  /** A semantic error highlighting exactly [token] (the number, without leading whitespace). */
  private fun outOfRange(grammar: Combinator, value: String, token: String = value) {
    val start = value.indexOf(token)
    assertVerdict(grammar, value, GrammarEngineVerdict(Kind.SEMANTIC, start, start + token.length))
  }

  private val ones64 = "1".repeat(64)

  // safe_atou16, as config_parse_uint16 reads [DHCPv4] ListenPort=.
  private val uint16 = wholeValue(
    SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 65_535L, acceptsWhitespace = true, acceptsMinus = true)
  )

  @Test
  fun testUint16AcceptsEverySpellingSystemdAccepts() {
    accepted(uint16, "5", "65535", "0", "00", "010", "0177777", "0xffff", "0XFF", "+0x10", "+5")
    // mangle_base's Python-style prefixes, in either case.
    accepted(uint16, "0b101", "0B11", "0o17", "0O17")
    // strtoul starts afresh after a 0b/0o prefix: a sign and whitespace may follow it.
    accepted(uint16, "0b+1", "0b-0", "0b 1", "0o 7", "0b\t1", "0b +1", "0b -0")
    // safe_atou_full only refuses a '-' when the value is not zero.
    accepted(uint16, "-0", "-00", "-0x0")
    // Leading whitespace: strspn(WHITESPACE), then strtoul's isspace() (which includes \v).
    accepted(uint16, " 5", "\u000B5")
  }

  @Test
  fun testUint16MalformedIsASyntaxErrorWhereStrtoulStopped() {
    malformed(uint16, "08", 1)       // octal: strtoul reads "0" and stops at '8'
    malformed(uint16, "0x", 1)       // "0x" without a hex digit is the number 0 followed by 'x'
    malformed(uint16, "0x1g", 3)
    malformed(uint16, "0x0x1", 3)
    malformed(uint16, "0b", 0)       // mangle_base strips "0b" and strtoul finds nothing
    malformed(uint16, "0o", 0)
    malformed(uint16, "0b2", 2)
    malformed(uint16, "0o8", 2)
    malformed(uint16, "0b0b1", 3)    // systemd turns off glibc's C23 "0b" support in strtoul
    malformed(uint16, "+0b1", 2)     // a sign hides the prefix from mangle_base; strtoul reads "+0"
    malformed(uint16, "-0b0", 2)
    malformed(uint16, "+-1", 1)
    malformed(uint16, "++1", 1)
    malformed(uint16, "+", 0)
    malformed(uint16, "-", 0)
    malformed(uint16, "5 ", 1)       // the whole string must be consumed
    malformed(uint16, "0 x1", 1)
    malformed(uint16, "abc", 0)
    malformed(uint16, "٣", 0)   // ARABIC-INDIC DIGIT THREE is not a C digit
  }

  @Test
  fun testUint16OutOfRangeIsASemanticErrorOnTheNumber() {
    outOfRange(uint16, "65536")
    outOfRange(uint16, "0x10000")
    outOfRange(uint16, "0200000")
    outOfRange(uint16, "-1")         // ERANGE, not EINVAL: strtoul parses it, safe_atou_full refuses the sign
    outOfRange(uint16, "0b-1")
    outOfRange(uint16, "0b -1")      // wraps to 2^64-1, which (unsigned) l != l refuses
    outOfRange(uint16, " 65536", "65536") // leading whitespace is its own token, not highlighted
  }

  // safe_atou32, as config_parse_uint32 reads [DHCPServer] PoolOffset=.
  private val uint32 = wholeValue(
    SafeAtoTerminal(
      signed = false, base = 0, flags = emptySet(), minInclusive = BigInteger.ZERO, maxInclusive = SafeAtoTerminal.UINT32_MAX,
      acceptsWhitespace = true, acceptsMinus = true,
    )
  )

  @Test
  fun testUint32BoundsAndWrapAround() {
    accepted(uint32, "4294967295", "0xffffffff", "037777777777")
    outOfRange(uint32, "4294967296")
    outOfRange(uint32, "0x100000000")
    outOfRange(uint32, "040000000000")
    outOfRange(uint32, "0b -1")
    // A '-' not at s[0] escapes the minus check and strtoul negates modulo 2^64: -(2^64-1) is 1, -(2^64-2) is 2.
    accepted(uint32, "0b -$ones64", "0o -1777777777777777777777", "0b -${"1".repeat(63)}0")
    // ...but -(2^63-2) wraps to 2^63+2, which does not fit, and a '-' right after the prefix is refused outright.
    outOfRange(uint32, "0b -${"1".repeat(62)}0")
    outOfRange(uint32, "0b-${"1".repeat(63)}0")
  }

  // safe_atou64 with the caller refusing 0, as config_parse_dhcp_max_attempts reads [DHCPv4] MaxAttempts=.
  private val uint64Positive = wholeValue(
    SafeAtoTerminal(
      signed = false, base = 0, flags = emptySet(), minInclusive = BigInteger.ONE, maxInclusive = SafeAtoTerminal.UINT64_MAX,
      acceptsWhitespace = true, acceptsMinus = true,
    )
  )

  @Test
  fun testUint64FullRange() {
    accepted(uint64Positive, "18446744073709551615", "0xffffffffffffffff", "01777777777777777777777", "0b$ones64", "\u000B+5")
    outOfRange(uint64Positive, "18446744073709551616")
    outOfRange(uint64Positive, "0x10000000000000000")
    outOfRange(uint64Positive, "02000000000000000000000")
    outOfRange(uint64Positive, "0b1$ones64")
    outOfRange(uint64Positive, "0")
    outOfRange(uint64Positive, "-0")
    outOfRange(uint64Positive, "-1")
    outOfRange(uint64Positive, " -1", "-1")
    outOfRange(uint64Positive, "0b-1")
  }

  @Test
  fun testUint64WrapAroundIsAccepted() {
    // 2^64-1 via strtoul's negation: '-' after whitespace (after the prefix, or after a \v/\f that WHITESPACE
    // does not cover) is not at s[0].
    accepted(uint64Positive, "0b -1", "0o -1", "\u000B-1", "\u000C-1", "0b -$ones64", "0b -${"1".repeat(63)}0")
    // A magnitude that overflows 64 bits is ERANGE even when negated.
    outOfRange(uint64Positive, "0b -1$ones64")
  }

  @Test
  fun testUint64PrefixAfterVerticalTabIsNotMangled() {
    // strspn(WHITESPACE) stops at \v, so mangle_base does not see the "0b"; strtoul then reads "0" and stops.
    malformed(uint64Positive, "\u000B0b1", 2)
    malformed(uint64Positive, "0x -1", 1)
    malformed(uint64Positive, "0b -0x1", 5)
    malformed(uint64Positive, "0b-", 0)
    malformed(uint64Positive, "0b -", 0)
    malformed(uint64Positive, "0b +", 0)
  }

  // safe_atou8, as config_parse_uint8 reads [GENEVE] TOS=.
  private val uint8 = wholeValue(
    SafeAtoTerminal(
      signed = false, base = 0, flags = emptySet(), minInclusive = BigInteger.ZERO, maxInclusive = SafeAtoTerminal.UINT8_MAX,
      acceptsWhitespace = true, acceptsMinus = true,
    )
  )

  @Test
  fun testUint8() {
    accepted(uint8, "255", "0xff", "0377", "-0")
    outOfRange(uint8, "256")
    outOfRange(uint8, "0x100")
    outOfRange(uint8, "0400")
    outOfRange(uint8, "0b -1")
  }

  // safe_atoi, as config_parse_int reads [Socket] Priority=.
  private val int32 = wholeValue(
    SafeAtoTerminal(
      signed = true, base = 0, flags = emptySet(), minInclusive = SafeAtoTerminal.INT32_MIN, maxInclusive = SafeAtoTerminal.INT32_MAX,
      acceptsWhitespace = true, acceptsMinus = true,
    )
  )

  @Test
  fun testInt32() {
    accepted(int32, "5", "-5", "+5", "-0", "-010", "2147483647", "-2147483648", "0x7fffffff", "-0x80000000", " -5", "\u000B-5")
    // A sign, and whitespace, may follow the 0b/0o prefix.
    accepted(int32, "0b-1", "0b -1", "0b+1", "0b 1", "0o-7", "0o -10", "0o\t-7")
    outOfRange(int32, "2147483648")
    outOfRange(int32, "-2147483649")
    outOfRange(int32, "0x80000000")
    outOfRange(int32, "-0x80000001")
    outOfRange(int32, "-9223372036854775808")
    outOfRange(int32, "0b11111111111111111111111111111111")
    outOfRange(int32, "0b -${"1".repeat(63)}0")
    malformed(int32, "-0b1", 2)
    malformed(int32, "+0o7", 2)
    malformed(int32, "08", 1)
    malformed(int32, "-08", 2)
    malformed(int32, "0x", 1)
    malformed(int32, "-0x", 2)
    malformed(int32, "-\u000B5", 1)
    malformed(int32, "- 5", 1)
  }

  // safe_atoli (safe_atolli on LP64), as config_parse_long reads [Socket] MessageQueueMaxMessages=.
  private val int64 = wholeValue(
    SafeAtoTerminal(
      signed = true, base = 0, flags = emptySet(), minInclusive = SafeAtoTerminal.INT64_MIN, maxInclusive = SafeAtoTerminal.INT64_MAX,
      acceptsWhitespace = true, acceptsMinus = true,
    )
  )

  @Test
  fun testInt64() {
    accepted(int64, "9223372036854775807", "-9223372036854775808", "0x7fffffffffffffff", "-0x8000000000000000", "0b -1")
    outOfRange(int64, "9223372036854775808")
    outOfRange(int64, "-9223372036854775809")
    outOfRange(int64, "0x8000000000000000")
  }

  // safe_atou32_full(…, 16, …), as config_parse_hierarchy_token_bucket_default_class reads [HierarchyTokenBucket] DefaultClass=.
  private val hexUint32 = wholeValue(
    SafeAtoTerminal(
      signed = false, base = 16, flags = emptySet(), minInclusive = BigInteger.ZERO, maxInclusive = SafeAtoTerminal.UINT32_MAX,
      acceptsWhitespace = true, acceptsMinus = true,
    )
  )

  @Test
  fun testBase16() {
    // No mangle_base in base 16: "0b1" is the hex number 0xb1, and an optional 0x is skipped by strtoul itself.
    accepted(hexUint32, "ff", "0xff", "0XFF", "ffffffff", "10", "+a", "-0", "-0x0", "0b1", " a")
    outOfRange(hexUint32, "100000000")
    outOfRange(hexUint32, "-1")
    outOfRange(hexUint32, "\u000B-1", "-1") // wraps to 2^64-1
    malformed(hexUint32, "0x", 1)
    malformed(hexUint32, "0o7", 1)
    malformed(hexUint32, "0x0x1", 3)
    malformed(hexUint32, "g", 0)
  }

  // safe_atoux64 with the caller's t <= INT64_MAX check, as config_parse_dhcp_pd_subnet_id reads [DHCPPrefixDelegation] SubnetId=.
  private val hexSubnetId = wholeValue(
    SafeAtoTerminal(
      signed = false, base = 16, flags = emptySet(), minInclusive = BigInteger.ZERO, maxInclusive = SafeAtoTerminal.INT64_MAX,
      acceptsWhitespace = true, acceptsMinus = true,
    )
  )

  @Test
  fun testBase16Uint64() {
    accepted(hexSubnetId, "7fffffffffffffff", "0x7fffffffffffffff", "-0")
    outOfRange(hexSubnetId, "8000000000000000")
    outOfRange(hexSubnetId, "ffffffffffffffff")
    outOfRange(hexSubnetId, "10000000000000000")
    outOfRange(hexSubnetId, "\u000B-1", "-1")
  }

  // parse_mode(): safe_atou_full(s, 8 | SAFE_ATO_REFUSE_PLUS_MINUS), then m <= 07777, as config_parse_mode reads [Service] UMask=.
  private val mode = wholeValue(
    SafeAtoTerminal(
      signed = false, base = 8, flags = setOf(SafeAtoTerminal.Flag.REFUSE_PLUS_MINUS), minInclusive = 0L, maxInclusive = 4095L,
      acceptsWhitespace = true, acceptsMinus = true,
    )
  )

  @Test
  fun testBase8WithRefusePlusMinus() {
    accepted(mode, "0022", "022", "22", "7777", "07777", " 22")
    // The flag only looks at s[0] after strspn(WHITESPACE), and \v is not in WHITESPACE.
    accepted(mode, "\u000B+22", "\u000B-0")
    outOfRange(mode, "10000")
    malformed(mode, "8", 0)
    malformed(mode, "0o22", 1)  // no mangle_base with an explicit base
    malformed(mode, "0x12", 1)
    malformed(mode, "0b1", 1)
    malformed(mode, "0 22", 1)
    malformed(mode, "+22", 0)
    malformed(mode, "-0", 0)
  }

  // parse_ip_port(): safe_atou16_full(s, SAFE_ATO_REFUSE_LEADING_WHITESPACE), then 0 refused, as config_parse_ip_port
  // reads [FooOverUDP] Port=.
  private val ipPort = wholeValue(
    SafeAtoTerminal(
      signed = false, base = 0, flags = setOf(SafeAtoTerminal.Flag.REFUSE_LEADING_WHITESPACE), minInclusive = 1L,
      maxInclusive = 65_535L, acceptsWhitespace = true, acceptsMinus = true,
    )
  )

  @Test
  fun testRefuseLeadingWhitespace() {
    // The masked base is still 0, so mangle_base still runs; \v is not in WHITESPACE, so the flag lets it through.
    accepted(ipPort, "5555", "65535", "0x15b3", "0b101", "+5", "\u000B5", "0b 1")
    outOfRange(ipPort, "0")
    outOfRange(ipPort, "-0")
    outOfRange(ipPort, "65536")
    malformed(ipPort, "\u000B0b1", 2)
  }

  // parse_uid(): safe_atou32_full(s, 10 | REFUSE_PLUS_MINUS | REFUSE_LEADING_ZERO | REFUSE_LEADING_WHITESPACE), then
  // uid_is_valid(), as config_parse_private_users reads the shift of .nspawn [Exec] PrivateUsers=.
  private val uid = wholeValue(
    SafeAtoTerminal(
      signed = false, base = 10,
      flags = setOf(
        SafeAtoTerminal.Flag.REFUSE_PLUS_MINUS,
        SafeAtoTerminal.Flag.REFUSE_LEADING_ZERO,
        SafeAtoTerminal.Flag.REFUSE_LEADING_WHITESPACE,
      ),
      minInclusive = 0L, maxInclusive = 4_294_967_294L, acceptsWhitespace = true, acceptsMinus = true,
    )
  )

  @Test
  fun testBase10WithEveryFlag() {
    // "0" itself survives REFUSE_LEADING_ZERO (sd-check: PrivateUsers=0:65536). uid_is_valid() also refuses 65535,
    // a hole a single range cannot express; that is the caller's business, not this terminal's.
    accepted(uid, "65536", "\u000B65536", "0")
    outOfRange(uid, "4294967295")
    malformed(uid, "065536", 1)   // a leading zero is refused unless the number is just "0"
    malformed(uid, "0x10000", 1)
    malformed(uid, "0b10000000000000000", 1)
    malformed(uid, "+65536", 0)
    malformed(uid, "-0", 0)
  }

  @Test
  fun testWithoutWhitespaceInsideAWhitespaceSeparatedList() {
    // In a word split on whitespace, "0b 1" is the invalid word "0b" followed by "1" (EINVAL).
    val number = SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 255L, acceptsWhitespace = false, acceptsMinus = true)
    val list = SequenceCombinator(number, ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), number)), EOF())
    accepted(list, "1", "1 2", "0b1 0o7 0x10 +5 -0")
    malformed(list, "0b 1", 2)
    outOfRange(list, "1 256", "256")
    outOfRange(list, "1 -1", "-1")
  }

  @Test
  fun testWithoutMinusAsTheLowerBoundOfARange() {
    // parse_range() cuts at the first '-', so the lower bound never sees one; the upper bound does.
    val lower = SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 65_535L, acceptsWhitespace = true, acceptsMinus = false)
    val upper = SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 65_535L, acceptsWhitespace = true, acceptsMinus = true)
    val range = SequenceCombinator(lower, ZeroOrOne(SequenceCombinator(LiteralChoiceTerminal("-"), upper)), EOF())
    accepted(range, "5", "5-10", "5--0", "0b1-0o7", "+1-+2", "1- 2")
    malformed(range, "-0", 0)
    malformed(range, "-5", 0)
    malformed(range, "0b-1-5", 2)
    outOfRange(range, "5--1", "-1")
  }

  @Test
  fun testColoringSkipsTheLeadingWhitespace() {
    val number = SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 255L, acceptsWhitespace = true, acceptsMinus = true)
    assertEquals(Role.LITERAL, defaultRole(number))
    assertEquals(listOf(Region(1, 3, Role.LITERAL)), SequenceCombinator(number, EOF()).colorize("\u000B17"))
    assertEquals(listOf(Region(0, 4, Role.LITERAL)), SequenceCombinator(number, EOF()).colorize("0b 1"))
  }

  @Test
  fun testClassicTokensSplitOffLeadingWhitespace() {
    val number = SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 255L, acceptsWhitespace = true, acceptsMinus = true)
    val match = number.SyntacticMatch("x  0x1f", 1)
    assertEquals(7, match.matchResult)
    assertEquals(listOf("  ", "0x1f"), match.tokens)
    assertEquals(listOf("WhitespaceTerminal", "SafeAtoTerminal"), match.terminals.map { it.javaClass.simpleName })
  }

  @Test
  fun testValueOfReturnsWhatSystemdStores() {
    val u32 = SafeAtoTerminal(
      signed = false, base = 0, flags = emptySet(), minInclusive = BigInteger.ZERO, maxInclusive = SafeAtoTerminal.UINT32_MAX,
      acceptsWhitespace = true, acceptsMinus = true,
    )
    assertEquals(BigInteger.valueOf(31), u32.valueOf("0x1f"))
    assertEquals(BigInteger.valueOf(5), u32.valueOf("0b+101"))
    assertEquals(BigInteger.valueOf(8), u32.valueOf("010"))
    assertEquals(BigInteger.ZERO, u32.valueOf("-0"))
    assertEquals(BigInteger.ONE, u32.valueOf("0b -$ones64"))
    assertEquals(BigInteger.valueOf(7), u32.valueOf(" 7"))
    assertEquals(null, u32.valueOf("4294967296"))
    assertEquals(null, u32.valueOf("-1"))
    assertEquals(null, u32.valueOf("08"))
    assertEquals(null, u32.valueOf("5 "))
    val i32 = SafeAtoTerminal(
      signed = true, base = 0, flags = emptySet(), minInclusive = SafeAtoTerminal.INT32_MIN, maxInclusive = SafeAtoTerminal.INT32_MAX,
      acceptsWhitespace = true, acceptsMinus = true,
    )
    assertEquals(BigInteger.valueOf(-8), i32.valueOf("0o -10"))
  }

  @Test
  fun testRejectsParametersSystemdHasNoHelperFor() {
    assertThrows(IllegalArgumentException::class.java) {
      SafeAtoTerminal(signed = false, base = 2, flags = emptySet(), minInclusive = 0L, maxInclusive = 1L, acceptsWhitespace = true, acceptsMinus = true)
    }
    assertThrows(IllegalArgumentException::class.java) {
      SafeAtoTerminal(
        signed = true, base = 0, flags = setOf(SafeAtoTerminal.Flag.REFUSE_PLUS_MINUS), minInclusive = 0L, maxInclusive = 1L,
        acceptsWhitespace = true, acceptsMinus = true,
      )
    }
    assertThrows(IllegalArgumentException::class.java) {
      SafeAtoTerminal(signed = true, base = 16, flags = emptySet(), minInclusive = 0L, maxInclusive = 1L, acceptsWhitespace = true, acceptsMinus = true)
    }
    assertThrows(IllegalArgumentException::class.java) {
      SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = -1L, maxInclusive = 1L, acceptsWhitespace = true, acceptsMinus = true)
    }
    assertThrows(IllegalArgumentException::class.java) {
      SafeAtoTerminal(
        signed = true, base = 0, flags = emptySet(), minInclusive = BigInteger.ZERO, maxInclusive = SafeAtoTerminal.UINT64_MAX,
        acceptsWhitespace = true, acceptsMinus = true,
      )
    }
    assertThrows(IllegalArgumentException::class.java) {
      SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 2L, maxInclusive = 1L, acceptsWhitespace = true, acceptsMinus = true)
    }
  }
}
