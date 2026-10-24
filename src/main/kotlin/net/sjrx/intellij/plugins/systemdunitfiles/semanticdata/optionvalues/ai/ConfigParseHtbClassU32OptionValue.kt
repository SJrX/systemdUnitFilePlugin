package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import java.math.BigInteger

/*
 * [HierarchyTokenBucketClass] Priority= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Priority=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/htb.c#L242-L293   config_parse_htb_class_u32 (TCLASS_KIND_HTB)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L403-L427   mangle_base
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L489   safe_atou16("+12345") accepted
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L466-L470   0o6 / 0B101 prefixes (safe_atolli, same mangle_base)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-htb-fifo.network#L18   Priority=1
 */

/**
 * Validator for [HierarchyTokenBucketClass] Priority=.
 *
 * C function: config_parse_htb_class_u32 (TCLASS_KIND_HTB). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * An unsigned 32-bit integer read with safe_atou32 (base 0, so `0b+1` and `0b 1` are 1; see
 * [SafeAtoTerminal]), 0…4294967295. No further bound.
 */
class ConfigParseHtbClassU32OptionValue : SimpleGrammarOptionValues(
  "config_parse_htb_class_u32",
  SequenceCombinator(
    SafeAtoTerminal(
      signed = false,
      base = 0,
      flags = emptySet(),
      minInclusive = BigInteger.ZERO,
      maxInclusive = SafeAtoTerminal.UINT32_MAX,
      acceptsWhitespace = true,
      acceptsMinus = true,
    ),
    EOF(),
  ),
)
