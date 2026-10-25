package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import com.intellij.codeInspection.ProblemsHolder
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [GenericRandomEarlyDetection] VirtualQueues=, DefaultVirtualQueue= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#VirtualQueues=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#DefaultVirtualQueue=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/gred.c#L68-L133   config_parse_gred_u32 (QDISC_KIND_GRED)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L403-L427   mangle_base
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/include/uapi/linux/pkt_sched.h#L305   MAX_DPs = 16
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L489   safe_atou16("+12345") accepted
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L466-L470   0o6 / 0B101 prefixes (safe_atolli, same mangle_base)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-gred.network#L12-L13   VirtualQueues=12, DefaultVirtualQueue=10
 */

/** A safe_atou32 number in [min]…[max]. */
private fun gredNumber(min: Long, max: Long) = SequenceCombinator(
  SafeAtoTerminal(
    signed = false,
    base = 0,
    flags = emptySet(),
    minInclusive = min,
    maxInclusive = max,
    acceptsWhitespace = true,
    acceptsMinus = true,
  ),
  EOF(),
)

/** VirtualQueues=: 0 always fails verify (DefaultVirtualQueue= is at least 0 and must be below it). */
private val GRED_VIRTUAL_QUEUES = object : GrammarOptionValue("config_parse_gred_u32", gredNumber(1L, 16L)) {
  override fun invalidValueMessage(key: String, badValue: String): String =
    "$key must be 1…16. systemd-networkd ignores values above 16, and with 0 DefaultVirtualQueue= can never be below " +
      "VirtualQueues=, so the whole [GenericRandomEarlyDetection] section is ignored."
}

/** DefaultVirtualQueue=: must be below VirtualQueues=, which is at most 16. */
private val GRED_DEFAULT_VIRTUAL_QUEUE = object : GrammarOptionValue("config_parse_gred_u32", gredNumber(0L, 15L)) {
  override fun invalidValueMessage(key: String, badValue: String): String =
    "$key must be 0…15 and below VirtualQueues= (at most 16); otherwise systemd-networkd ignores the whole " +
      "[GenericRandomEarlyDetection] section."
}

/**
 * Validator for [GenericRandomEarlyDetection] VirtualQueues=, DefaultVirtualQueue=.
 *
 * C function: config_parse_gred_u32 (QDISC_KIND_GRED). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * An unsigned integer read with safe_atou32 (base 0: decimal, `0x` hex, `0o`/leading-zero octal, `0b`
 * binary, optional `+`; `0b+1` and `0b 1` are 1, see [SafeAtoTerminal]) that must not exceed MAX_DPs (16). Above 16 the parser logs "Invalid ..., ignoring
 * assignment" (although, at 0f0682b773, it still stores the value — a C bug; the kernel refuses more than
 * 16 DPs anyway), so it is flagged here.
 *
 * generic_random_early_detection_verify() then drops the whole section unless DefaultVirtualQueue= is below
 * VirtualQueues= (src/network/tc/gred.c#L56-L65), so `VirtualQueues=0` and `DefaultVirtualQueue=16` can never
 * work and are flagged per key (#565). Whether the two values fit together is not checked (that needs both lines).
 */
class ConfigParseGredU32OptionValue : SimpleGrammarOptionValues(
  "config_parse_gred_u32",
  gredNumber(0L, 16L),
) {
  override fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
    when (property.key) {
      "VirtualQueues" -> GRED_VIRTUAL_QUEUES.generateProblemDescriptors(property, holder)
      "DefaultVirtualQueue" -> GRED_DEFAULT_VIRTUAL_QUEUE.generateProblemDescriptors(property, holder)
      else -> super.generateProblemDescriptors(property, holder)
    }
  }
}
