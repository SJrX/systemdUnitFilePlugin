package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [HierarchyTokenBucket] RateToQuantum= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#RateToQuantum=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/htb.c#L100-L150   config_parse_htb_u32 (QDISC_KIND_HTB)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L475   safe_atou_full
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L403-L427   mangle_base
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L489   safe_atou16("+12345") accepted
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L466-L470   0o6 / 0B101 prefixes (safe_atolli, same mangle_base)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-htb-fifo.network#L13   RateToQuantum=20
 */

/**
 * Validator for [HierarchyTokenBucket] RateToQuantum=.
 *
 * C function: config_parse_htb_u32 (QDISC_KIND_HTB). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * An unsigned 32-bit integer read with safe_atou32 (base 0), 0…4294967295. No further bound.
 */
class ConfigParseHtbU32OptionValue : SimpleGrammarOptionValues(
  "config_parse_htb_u32",
  safeAtou32Value(),
)
