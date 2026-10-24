package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import java.math.BigInteger

/*
 * [CAN] TimeQuantaNSec=, DataTimeQuantaNSec= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#TimeQuantaNSec=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-can.c#L173-L208   config_parse_can_time_quanta
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1320-L1466   extract_nsec_multiplier / parse_nsec
 */

// extract_nsec_multiplier()'s table, in table order (the C takes the first suffix the text starts with,
// and so does a regex alternation). A missing suffix means nanoseconds.
private const val NSEC_SUFFIXES =
  "seconds|second|sec|s|minutes|minute|min|months|month|M|msec|ms|m|hours|hour|hr|h|days|day|d|" +
    "weeks|week|w|years|year|y|usec|us|μs|µs|nsec|ns"

private val NSEC_ELEMENT = Regex("""\s*\+?([0-9]*)(?:\.([0-9]*))?\s*($NSEC_SUFFIXES)?""")

private val NSEC_MULTIPLIERS: Map<String, BigInteger> = run {
  val sec = 1_000_000_000L
  val minute = 60 * sec
  val hour = 60 * minute
  val day = 24 * hour
  mapOf(
    "seconds" to sec, "second" to sec, "sec" to sec, "s" to sec,
    "minutes" to minute, "minute" to minute, "min" to minute, "m" to minute,
    "months" to 2_629_800L * sec, "month" to 2_629_800L * sec, "M" to 2_629_800L * sec,
    "msec" to 1_000_000L, "ms" to 1_000_000L,
    "hours" to hour, "hour" to hour, "hr" to hour, "h" to hour,
    "days" to day, "day" to day, "d" to day,
    "weeks" to 7 * day, "week" to 7 * day, "w" to 7 * day,
    "years" to 31_557_600L * sec, "year" to 31_557_600L * sec, "y" to 31_557_600L * sec,
    "usec" to 1000L, "us" to 1000L, "μs" to 1000L, "µs" to 1000L,
    "nsec" to 1L, "ns" to 1L,
  ).mapValues { BigInteger.valueOf(it.value) }
}

/**
 * What parse_nsec() gives a finite [text], in nanoseconds, or null if it cannot be evaluated. Like the C,
 * each fractional digit adds digit × (multiplier / 10^k) with integer division, so `1.5` (nanoseconds) is 1.
 * Values parse_nsec() would refuse with -ERANGE come out larger than UINT32_MAX, so the range check below
 * refuses them too.
 */
private fun parseNsecValue(text: String): BigInteger? {
  var pos = 0
  var total = BigInteger.ZERO
  while (pos < text.length) {
    while (pos < text.length && text[pos].isWhitespace()) pos++
    if (pos >= text.length) break
    val m = NSEC_ELEMENT.matchAt(text, pos) ?: return null
    if (m.value.isBlank()) return null
    pos += m.value.length
    val multiplier = m.groupValues[3].takeIf { it.isNotEmpty() }?.let { NSEC_MULTIPLIERS[it] } ?: BigInteger.ONE
    total += BigInteger(m.groupValues[1].ifEmpty { "0" }) * multiplier
    var step = multiplier / BigInteger.TEN
    for (digit in m.groupValues[2]) {
      total += BigInteger.valueOf((digit - '0').toLong()) * step
      step /= BigInteger.TEN
    }
  }
  return total
}

/**
 * Validator for [CAN] TimeQuantaNSec= and DataTimeQuantaNSec= (.network).
 *
 * C Function: config_parse_can_time_quanta(0). The value is read with parse_nsec() (one or more
 * `number[unit]` elements, a bare number meaning nanoseconds; see [NSEC_DURATION_FINITE]) and must then
 * satisfy `0 < val <= UINT32_MAX` nanoseconds. So `infinity` (NSEC_INFINITY), `0` and `4294967296` are
 * refused ("Time quanta out of permitted range 1...4294967295"), while `4.2s` and `1ms 500us` are accepted
 * (sd-check at 0f0682b773).
 */
class ConfigParseCanTimeQuantaOptionValue : SimpleGrammarOptionValues(
  "config_parse_can_time_quanta",
  SequenceCombinator(
    Checked(NSEC_DURATION_FINITE) { text ->
      parseNsecValue(text)?.let { it > BigInteger.ZERO && it <= SafeAtoTerminal.UINT32_MAX } ?: true
    },
    EOF(),
  ),
)
