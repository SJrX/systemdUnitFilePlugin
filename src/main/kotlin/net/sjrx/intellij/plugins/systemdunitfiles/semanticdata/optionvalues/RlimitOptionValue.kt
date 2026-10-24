package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import java.math.BigInteger

/**
 * Limit*= settings: config_parse_rlimit() → rlimit_parse().
 *
 * https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L1768-L1803
 * https://github.com/systemd/systemd/blob/0f0682b773/src/basic/rlimit-util.c#L79-L292
 *
 * The value is `soft` or `soft:hard`, cut at the first ':' without trimming, so whitespace around a field reaches
 * the field's parser: parse_size() and parse_time() skip it, safe_atou64() only skips leading whitespace, and the
 * literal `infinity` of the size and number fields must match exactly. The soft limit must not be above the hard
 * limit (-EILSEQ).
 */
class RlimitOptionValue(grammar : Combinator) : GrammarOptionValue("config_parse_rlimit", grammar) {
  companion object {

    /** RLIM_INFINITY; every finite field must stay below it. */
    private val RLIM_INFINITY: BigInteger = SafeAtoTerminal.UINT64_MAX

    private val USEC_PER_SEC: BigInteger = BigInteger.valueOf(1_000_000L)

    // rlimit_parse_u64(): `infinity` or safe_atou64() below RLIM_INFINITY.
    private val U64_NUMBER = SafeAtoTerminal(
      signed = false,
      base = 0,
      flags = emptySet(),
      minInclusive = BigInteger.ZERO,
      maxInclusive = RLIM_INFINITY - BigInteger.ONE,
      acceptsWhitespace = true,
      acceptsMinus = true,
    )

    val GENERIC_SEQ = PreferValidAlternativeCombinator(
      LiteralChoiceTerminal("infinity"),
      U64_NUMBER,
    )

    // rlimit_parse_size(): `infinity` or parse_size(…, 1024) below RLIM_INFINITY. parse_size() skips whitespace before
    // a number but not after the last element, so a soft limit such as "12 G " (before the ':') is refused.
    val BYTE_SEQ = PreferValidAlternativeCombinator(
      LiteralChoiceTerminal("infinity"),
      ValueChecked(
        SequenceCombinator(
          ZeroOrOne(WhitespaceTerminal()),
          parseSize(base = 1024, minInclusive = BigInteger.ZERO, maxInclusive = RLIM_INFINITY - BigInteger.ONE, allowInnerWhitespace = true),
        )
      ) { text -> text.last() !in WHITESPACE },
    )

    // rlimit_parse_sec() / rlimit_parse_usec(): parse_time(). The structure locates the parts; the ValueChecked
    // around it runs the exact parse_time() rules (time-util.c#L1198-L1287).
    private val TIME_ELEMENT = SequenceCombinator(
      RegexTerminal("""\+?[0-9]+(?:\.[0-9]+)?|\.[0-9]+""", """\+?[0-9]+(?:\.[0-9]+)?|\.[0-9]+"""),
      ZeroOrOne(WhitespaceTerminal()),
      ZeroOrOne(
        LiteralChoiceTerminal(
          "seconds", "second", "sec", "s", "minutes", "minute", "min", "months", "month", "M", "msec", "ms", "m",
          "hours", "hour", "hr", "h", "days", "day", "d", "weeks", "week", "w", "years", "year", "y", "usec", "us", "μs", "µs",
        )
      ),
      ZeroOrOne(WhitespaceTerminal()),
    )

    val TIME_SEQ = ValueChecked(
      SequenceCombinator(
        ZeroOrOne(WhitespaceTerminal()),
        PreferValidAlternativeCombinator(
          SequenceCombinator(LiteralChoiceTerminal("infinity"), ZeroOrOne(WhitespaceTerminal())),
          OneOrMore(TIME_ELEMENT),
        ),
      )
    ) { text -> parseTime(text, USEC_PER_SEC) != null }

    // The same for LimitRTTIME=, whose parse_time() default unit is the microsecond.
    val USEC_SEQ = ValueChecked(
      SequenceCombinator(
        ZeroOrOne(WhitespaceTerminal()),
        PreferValidAlternativeCombinator(
          SequenceCombinator(LiteralChoiceTerminal("infinity"), ZeroOrOne(WhitespaceTerminal())),
          OneOrMore(TIME_ELEMENT),
        ),
      )
    ) { text -> parseTime(text, BigInteger.ONE) != null }

    // rlimit_parse_nice(): "+n" is the nice level n (0…19), "-n" the nice level -n (0…20), and an unprefixed
    // value is the raw limit (0…40).
    val NICE_SEQ = PreferValidAlternativeCombinator(
      SequenceCombinator(
        LiteralChoiceTerminal("+"),
        SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 19L, acceptsWhitespace = true, acceptsMinus = true),
      ),
      SequenceCombinator(
        LiteralChoiceTerminal("-"),
        SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 20L, acceptsWhitespace = true, acceptsMinus = true),
      ),
      SafeAtoTerminal(
        signed = false,
        base = 0,
        flags = setOf(SafeAtoTerminal.Flag.REFUSE_PLUS_MINUS),
        minInclusive = 0L,
        maxInclusive = 40L,
        acceptsWhitespace = true,
        acceptsMinus = true,
      ),
    )

    val COLON = LiteralChoiceTerminal(":")

    val BYTE_RLIMIT = SequenceCombinator(
      ValueChecked(SequenceCombinator(BYTE_SEQ, ZeroOrOne(SequenceCombinator(COLON, BYTE_SEQ)))) { text ->
        softNotAboveHard(text) { field -> if (field == "infinity") RLIM_INFINITY else evaluateParseSize(field, 1024) }
      },
      EOF(),
    )

    val TIME_RLIMIT = SequenceCombinator(
      ValueChecked(SequenceCombinator(TIME_SEQ, ZeroOrOne(SequenceCombinator(COLON, TIME_SEQ)))) { text ->
        // rlimit_parse_sec() rounds up to whole seconds before the comparison.
        softNotAboveHard(text) { field ->
          parseTime(field, USEC_PER_SEC)?.let { usec ->
            if (usec == RLIM_INFINITY) RLIM_INFINITY else (usec + USEC_PER_SEC - BigInteger.ONE) / USEC_PER_SEC
          }
        }
      },
      EOF(),
    )

    // LimitRTTIME= uses parse_time() with microseconds as the default unit.
    val USEC_RLIMIT = SequenceCombinator(
      ValueChecked(SequenceCombinator(USEC_SEQ, ZeroOrOne(SequenceCombinator(COLON, USEC_SEQ)))) { text ->
        softNotAboveHard(text) { field -> parseTime(field, BigInteger.ONE) }
      },
      EOF(),
    )

    val GENERIC_RLIMIT = SequenceCombinator(
      ValueChecked(SequenceCombinator(GENERIC_SEQ, ZeroOrOne(SequenceCombinator(COLON, GENERIC_SEQ)))) { text ->
        softNotAboveHard(text) { field -> if (field == "infinity") RLIM_INFINITY else U64_NUMBER.valueOf(field) }
      },
      EOF(),
    )

    // What rlimit_parse_nice() stores for a field; the unprefixed form is plain safe_atou64() (no flags).
    private val NICE_RAW_NUMBER = SafeAtoTerminal(
      signed = false,
      base = 0,
      flags = emptySet(),
      minInclusive = 0L,
      maxInclusive = 40L,
      acceptsWhitespace = true,
      acceptsMinus = true,
    )

    val NICE_RLIMIT = SequenceCombinator(
      ValueChecked(SequenceCombinator(NICE_SEQ, ZeroOrOne(SequenceCombinator(COLON, NICE_SEQ)))) { text ->
        softNotAboveHard(text) { field ->
          val twenty = BigInteger.valueOf(20)
          when {
            field.startsWith("+") -> NICE_RAW_NUMBER.valueOf(field.substring(1))?.let { twenty - it }
            field.startsWith("-") -> NICE_RAW_NUMBER.valueOf(field.substring(1))?.let { twenty + it }
            else -> NICE_RAW_NUMBER.valueOf(field)
          }
        }
      },
      EOF(),
    )

    /**
     * False if [text] is `soft:hard` and the soft limit is above the hard one (rlimit_parse()'s -EILSEQ); null when a
     * field cannot be evaluated, which leaves the verdict to the field grammars.
     */
    private fun softNotAboveHard(text: String, evaluate: (String) -> BigInteger?): Boolean? {
      val colon = text.indexOf(':')
      if (colon < 0) return true
      val soft = evaluate(text.substring(0, colon)) ?: return null
      val hard = evaluate(text.substring(colon + 1)) ?: return null
      return soft <= hard
    }

    private const val WHITESPACE = " \t\n\r"

    // extract_multiplier()'s table, in table order (time-util.c#L1147-L1196).
    private val TIME_MULTIPLIERS: List<Pair<String, Long>> = listOf(
      "seconds" to 1_000_000L, "second" to 1_000_000L, "sec" to 1_000_000L, "s" to 1_000_000L,
      "minutes" to 60_000_000L, "minute" to 60_000_000L, "min" to 60_000_000L,
      "months" to 2_629_800_000_000L, "month" to 2_629_800_000_000L, "M" to 2_629_800_000_000L,
      "msec" to 1_000L, "ms" to 1_000L, "m" to 60_000_000L,
      "hours" to 3_600_000_000L, "hour" to 3_600_000_000L, "hr" to 3_600_000_000L, "h" to 3_600_000_000L,
      "days" to 86_400_000_000L, "day" to 86_400_000_000L, "d" to 86_400_000_000L,
      "weeks" to 604_800_000_000L, "week" to 604_800_000_000L, "w" to 604_800_000_000L,
      "years" to 31_557_600_000_000L, "year" to 31_557_600_000_000L, "y" to 31_557_600_000_000L,
      "usec" to 1L, "us" to 1L, "μs" to 1L, "µs" to 1L,
    )

    /**
     * parse_time(t, &usec, defaultUnit), https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287.
     * Returns the value in microseconds (USEC_INFINITY for `infinity`), or null for -EINVAL / -ERANGE.
     */
    private fun parseTime(t: String, defaultUnit: BigInteger): BigInteger? {
      val usecInfinity = RLIM_INFINITY
      val llongMax = BigInteger.valueOf(Long.MAX_VALUE)
      val n = t.length
      var p = 0
      while (p < n && t[p] in WHITESPACE) p++
      if (t.startsWith("infinity", p)) {
        return if (t.substring(p + "infinity".length).all { it in WHITESPACE }) usecInfinity else null
      }

      var usec = BigInteger.ZERO
      var something = false
      while (true) {
        var multiplier = defaultUnit
        while (p < n && t[p] in WHITESPACE) p++
        if (p >= n) {
          return if (something) usec else null
        }
        if (t[p] == '-') return null

        // strtoll(p, &e, 10); a leading '-' was refused above.
        var q = p
        if (q < n && t[q] == '+') q++
        val digitsStart = q
        while (q < n && t[q] in '0'..'9') q++
        val e = if (q == digitsStart) p else q
        val l = if (q == digitsStart) BigInteger.ZERO else BigInteger(t.substring(digitsStart, q))
        if (l > llongMax) return null

        if (e < n && t[e] == '.') {
          p = e + 1
          while (p < n && t[p] in '0'..'9') p++
        } else if (e == p) {
          return null
        } else {
          p = e
        }

        var s = p
        while (s < n && t[s] in WHITESPACE) s++
        val suffix = TIME_MULTIPLIERS.firstOrNull { t.startsWith(it.first, s) }
        if (suffix != null) {
          multiplier = BigInteger.valueOf(suffix.second)
          s += suffix.first.length
        }
        if (s == p && s < n) return null
        p = s

        if (l >= usecInfinity / multiplier) return null
        var k = l * multiplier
        if (k >= usecInfinity - usec) return null
        usec += k
        something = true

        if (e < n && t[e] == '.') {
          var m = multiplier / BigInteger.TEN
          var b = e + 1
          while (b < n && t[b] in '0'..'9') {
            k = BigInteger.valueOf((t[b] - '0').toLong()) * m
            if (k >= usecInfinity - usec) return null
            usec += k
            b++
            m /= BigInteger.TEN
          }
          if (b == e + 1) return null
        }
      }
    }

    val validators = mapOf(
      //        Exec.LimitCPU,                config_parse_rlimit,         RLIMIT_CPU,               offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_CPU") to RlimitOptionValue(TIME_RLIMIT),
      //        Exec.LimitFSIZE,              config_parse_rlimit,         RLIMIT_FSIZE,             offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_FSIZE") to RlimitOptionValue(BYTE_RLIMIT),
      //        Exec.LimitDATA,               config_parse_rlimit,         RLIMIT_DATA,              offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_DATA") to RlimitOptionValue(BYTE_RLIMIT),
      //        Exec.LimitSTACK,              config_parse_rlimit,         RLIMIT_STACK,             offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_STACK") to RlimitOptionValue(BYTE_RLIMIT),
      //        Exec.LimitCORE,               config_parse_rlimit,         RLIMIT_CORE,              offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_CORE") to RlimitOptionValue(BYTE_RLIMIT),
      //        Exec.LimitRSS,                config_parse_rlimit,         RLIMIT_RSS,               offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_RSS") to RlimitOptionValue(BYTE_RLIMIT),
      //        Exec.LimitNOFILE,             config_parse_rlimit,         RLIMIT_NOFILE,            offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_NOFILE") to RlimitOptionValue(GENERIC_RLIMIT),
      //        Exec.LimitAS,                 config_parse_rlimit,         RLIMIT_AS,                offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_AS") to RlimitOptionValue(BYTE_RLIMIT),
      //        Exec.LimitNPROC,              config_parse_rlimit,         RLIMIT_NPROC,             offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_NPROC") to RlimitOptionValue(GENERIC_RLIMIT),
      //        Exec.LimitMEMLOCK,            config_parse_rlimit,         RLIMIT_MEMLOCK,           offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_MEMLOCK") to RlimitOptionValue(BYTE_RLIMIT),
      //        Exec.LimitLOCKS,              config_parse_rlimit,         RLIMIT_LOCKS,             offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_LOCKS") to RlimitOptionValue(GENERIC_RLIMIT),
      //        Exec.LimitSIGPENDING,         config_parse_rlimit,         RLIMIT_SIGPENDING,        offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_SIGPENDING") to RlimitOptionValue(GENERIC_RLIMIT),
      //        Exec.LimitMSGQUEUE,           config_parse_rlimit,         RLIMIT_MSGQUEUE,          offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_MSGQUEUE") to RlimitOptionValue(BYTE_RLIMIT),
      //        Exec.LimitNICE,               config_parse_rlimit,         RLIMIT_NICE,              offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_NICE") to RlimitOptionValue(NICE_RLIMIT),
      //        Exec.LimitRTPRIO,             config_parse_rlimit,         RLIMIT_RTPRIO,            offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_RTPRIO") to RlimitOptionValue(GENERIC_RLIMIT),
      //        Exec.LimitRTTIME,             config_parse_rlimit,         RLIMIT_RTTIME,            offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_RTTIME") to RlimitOptionValue(USEC_RLIMIT),
    )
  }
}
