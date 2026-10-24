package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import java.math.BigInteger

/*
 * [HierarchyTokenBucket] DefaultClass= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#DefaultClass=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/htb.c#L48-L98   config_parse_htb_default_class (QDISC_KIND_HTB)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.h#L61-L64   safe_atou32_full(rvalue, 16, ..)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full (explicit base: mangle_base is skipped)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-htb-fifo.network#L12   DefaultClass=30 (i.e. 0x30)
 */

/**
 * Validator for [HierarchyTokenBucket] DefaultClass=.
 *
 * C function: config_parse_htb_default_class (QDISC_KIND_HTB). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * The default class's minor id, always read as HEXADECIMAL (safe_atou32_full(.., 16)): `30` is 0x30, `0x30`
 * is the same, `ff` is legal, `0b1` is read as hex 0xb1, and `0o7` is rejected (with an explicit base,
 * mangle_base does not run). strtoul skips a leading vertical tab, so `\v5` is 5 (see [SafeAtoTerminal]).
 * Range 0…0xffffffff.
 */
class ConfigParseHtbDefaultClassOptionValue : SimpleGrammarOptionValues(
  "config_parse_htb_default_class",
  SequenceCombinator(
    SafeAtoTerminal(
      signed = false,
      base = 16,
      flags = emptySet(),
      minInclusive = BigInteger.ZERO,
      maxInclusive = SafeAtoTerminal.UINT32_MAX,
      acceptsWhitespace = true,
      acceptsMinus = true,
    ),
    EOF(),
  ),
)
