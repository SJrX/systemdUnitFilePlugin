package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * [Bridge] FDBMaxLearned= (.netdev)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#FDBMaxLearned=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/bridge.c#L250-L282   config_parse_bridge_fdb_max_learned → config_parse_uint32_bounded(0, UINT32_MAX)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full (safe_atou32 / safe_atou)
 */

/**
 * Validator for [Bridge] FDBMaxLearned= (.netdev).
 *
 * C Function: config_parse_bridge_fdb_max_learned(0). An empty assignment unsets it and is handled
 * before this grammar runs. config_parse_uint32_bounded(…, 0, UINT32_MAX, …) reads the value with
 * safe_atou() (base 0), so `0x100`, `0o400` and `0b1` are accepted (sd-check at 0f0682b773).
 */
class ConfigParseBridgeFdbMaxLearnedOptionValue : SimpleGrammarOptionValues(
  "config_parse_bridge_fdb_max_learned",
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
