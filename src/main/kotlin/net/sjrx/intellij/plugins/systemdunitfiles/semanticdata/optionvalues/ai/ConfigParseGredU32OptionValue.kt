package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

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

/**
 * Validator for [GenericRandomEarlyDetection] VirtualQueues=, DefaultVirtualQueue=.
 *
 * C function: config_parse_gred_u32 (QDISC_KIND_GRED). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * An unsigned integer read with safe_atou32 (base 0: decimal, `0x` hex, `0o`/leading-zero octal, `0b`
 * binary, optional `+`) that must not exceed MAX_DPs (16). Above 16 the parser logs "Invalid ..., ignoring
 * assignment" (although, at 0f0682b773, it still stores the value — a C bug; the kernel refuses more than
 * 16 DPs anyway), so it is flagged here.
 *
 * Lenient: both keys share the parser, so 0..16 is accepted for both, although VirtualQueues=0 and
 * DefaultVirtualQueue=16 always fail generic_random_early_detection_verify (DefaultVirtualQueue= must
 * be less than VirtualQueues=). The man page documents VirtualQueues= as 1…16.
 */
class ConfigParseGredU32OptionValue : SimpleGrammarOptionValues(
  "config_parse_gred_u32",
  SequenceCombinator(qdiscSafeAtou(17), EOF()),
)
