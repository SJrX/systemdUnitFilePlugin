package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import java.math.BigInteger

/*
 * [FairQueueingControlledDelay] PacketLimit=, Flows= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#PacketLimit=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Flows=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/fq-codel.c#L97-L155   config_parse_fq_codel_u32 (QDISC_KIND_FQ_CODEL)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L403-L427   mangle_base
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L489   safe_atou16("+12345") accepted
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L466-L470   0o6 / 0B101 prefixes (safe_atolli, same mangle_base)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-fq_codel.network#L12-L14   PacketLimit=20480, Flows=2048
 */

/**
 * Validator for [FairQueueingControlledDelay] PacketLimit=, Flows=.
 *
 * C function: config_parse_fq_codel_u32 (QDISC_KIND_FQ_CODEL). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * An unsigned 32-bit integer read with safe_atou32 (base 0: decimal, `0x` hex, `0o`/leading-zero
 * octal, `0b` binary, optional `+`), 0…4294967295. No further bound in the parser. strtoul starts
 * afresh after a `0b`/`0o` prefix, so `0b+1` and `0b 1` are 1 (see [SafeAtoTerminal]).
 */
class ConfigParseFqCodelU32OptionValue : SimpleGrammarOptionValues(
  "config_parse_fq_codel_u32",
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
