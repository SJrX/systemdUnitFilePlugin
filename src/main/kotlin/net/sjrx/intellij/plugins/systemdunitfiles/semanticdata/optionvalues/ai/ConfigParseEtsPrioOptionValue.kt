package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

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

private val ETS_BAND = safeAtou(256)

/**
 * Validator for [EnhancedTransmissionSelection] PriorityMap=.
 *
 * C function: config_parse_ets_prio (QDISC_KIND_ETS). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * A whitespace-separated list of band numbers, each read with safe_atou8 (base 0, 0…255), at most
 * TC_PRIO_MAX + 1 (16) entries per line. Out-of-range or unparsable entries are logged and skipped by
 * the C, and so are flagged here; so is a 17th entry.
 *
 * Lenient: enhanced_transmission_selection_verify later rejects the whole section if any entry is not
 * below the number of bands (at most 16), so values 16…255 can never actually work; they are accepted
 * here because the man page documents 0…255 and that is what the parser itself checks. The 16-entry
 * cap is cumulative across lines; only the per-line count is checked.
 */
class ConfigParseEtsPrioOptionValue : SimpleGrammarOptionValues(
  "config_parse_ets_prio",
  SequenceCombinator(ETS_BAND, Repeat(SequenceCombinator(WhitespaceTerminal(), ETS_BAND), 0, 15), EOF()),
)
