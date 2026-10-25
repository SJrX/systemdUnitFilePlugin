package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import java.math.BigInteger

/*
 * [EnhancedTransmissionSelection] PriorityMap= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#PriorityMap=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/ets.c#L227-L302   config_parse_ets_prio (QDISC_KIND_ETS)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L403-L427   mangle_base
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.h#L45-L47   safe_atou8
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L583-L597   safe_atou8_full (rejects > UINT8_MAX)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/include/uapi/linux/pkt_sched.h#L28   TC_PRIO_MAX = 15
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L489   safe_atou16("+12345") accepted
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L466-L470   0o6 / 0B101 prefixes (safe_atolli, same mangle_base)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-ets.network#L18-L21   PriorityMap=8 7 6 5 / PriorityMap= / PriorityMap=3 4 5
 */

private val ETS_BAND = SafeAtoTerminal(
  signed = false,
  base = 0,
  flags = emptySet(),
  minInclusive = BigInteger.ZERO,
  // safe_atou8 allows up to 255, but see the class comment: only 0…15 can ever be below the band count.
  maxInclusive = BigInteger.valueOf(15),
  acceptsWhitespace = false,
  acceptsMinus = true,
)

/**
 * Validator for [EnhancedTransmissionSelection] PriorityMap=.
 *
 * C function: config_parse_ets_prio (QDISC_KIND_ETS). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * A whitespace-separated list of band numbers, each read with safe_atou8 (base 0, 0…255), at most
 * TC_PRIO_MAX + 1 (16) entries per line. Each entry is a word from extract_first_word(), so the number
 * cannot contain a blank: `0b+1` is 1, but `0b 1` is the words `0b` (invalid) and `1`. Out-of-range or unparsable entries are logged and skipped by
 * the C, and so are flagged here; so is a 17th entry.
 *
 * The parser itself takes 0…255 (and the man page says so), but enhanced_transmission_selection_verify()
 * drops the whole section when an entry is not below the number of bands (src/network/tc/ets.c#L325-L330),
 * and an ETS qdisc has at most 16 bands (Bands= is capped at TCQ_ETS_MAX_BANDS, ets.c#L131, and the kernel
 * refuses more), so 16…255 never work and are flagged (#565). The 16-entry cap is cumulative across lines;
 * only the per-line count is checked.
 */
class ConfigParseEtsPrioOptionValue : SimpleGrammarOptionValues(
  "config_parse_ets_prio",
  SequenceCombinator(ETS_BAND, Repeat(SequenceCombinator(WhitespaceTerminal(), ETS_BAND), 0, 15), EOF()),
) {
  // Only a well-formed number above 15 gets the band message; other bad words (`-1`, `0b`) keep the generic one.
  override fun invalidValueMessage(key: String, badValue: String): String =
    if ((etsNumber(badValue) ?: 0) > 15) {
      "$key: band '$badValue' is out of range. Bands are numbered 0…15 (an ETS qdisc has at most 16), and systemd-networkd " +
        "ignores the whole [EnhancedTransmissionSelection] section when a PriorityMap= entry is not below the number of bands."
    } else {
      super.invalidValueMessage(key, badValue)
    }
}

/** The value of a plain unsigned safe_atou8 word in C base-0 notation (`16`, `0x10`, `020`), or null for anything else. */
private fun etsNumber(word: String): Long? {
  val digits = word.removePrefix("+")
  return when {
    digits.startsWith("0x") || digits.startsWith("0X") -> digits.substring(2).toLongOrNull(16)
    digits.length > 1 && digits.startsWith("0") -> digits.substring(1).toLongOrNull(8)
    else -> digits.toLongOrNull(10)
  }?.takeIf { it >= 0 && digits.none { c -> c == '-' || c == '+' } }
}
