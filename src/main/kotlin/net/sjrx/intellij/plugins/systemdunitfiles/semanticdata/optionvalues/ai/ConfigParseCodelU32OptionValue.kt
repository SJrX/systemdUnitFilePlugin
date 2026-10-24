package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * [ControlledDelay] PacketLimit= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#PacketLimit=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/codel.c#L77-L127   config_parse_codel_u32
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full (safe_atou32 / safe_atou)
 */

/**
 * Validator for [ControlledDelay] PacketLimit= (.network).
 *
 * C Function: config_parse_codel_u32(QDISC_KIND_CODEL). An empty assignment resets it and is handled
 * before this grammar runs. The value is read with safe_atou32() (base 0) and nothing else is checked,
 * so the whole uint32 range is accepted, including 4294967295 (sd-check at 0f0682b773; the old
 * validator stopped at 4294967294).
 */
class ConfigParseCodelU32OptionValue : SimpleGrammarOptionValues(
  "config_parse_codel_u32",
  SequenceCombinator(
    SafeAtoTerminal(
      signed = false,
      base = 0,
      flags = emptySet(),
      minInclusive = 0L,
      maxInclusive = 4_294_967_295L,
      acceptsWhitespace = true,
      acceptsMinus = true,
    ),
    EOF(),
  ),
)
