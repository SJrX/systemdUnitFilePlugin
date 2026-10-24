package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * [PFIFO] PacketLimit= and [PFIFOHeadDrop] PacketLimit= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#PacketLimit=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/fifo.c#L43-L101   config_parse_pfifo_size
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full (safe_atou32 / safe_atou)
 */

/**
 * Validator for [PFIFO] PacketLimit= and [PFIFOHeadDrop] PacketLimit= (.network).
 *
 * C Function: config_parse_pfifo_size(QDISC_KIND_PFIFO / QDISC_KIND_PFIFO_HEAD_DROP). An empty assignment
 * resets it and is handled before this grammar runs. The value is read with safe_atou32() (base 0, so
 * `0x100` is accepted; sd-check at 0f0682b773) and nothing else is checked.
 */
class ConfigParsePfifoSizeOptionValue : SimpleGrammarOptionValues(
  "config_parse_pfifo_size",
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
