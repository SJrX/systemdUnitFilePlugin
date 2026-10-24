package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * [Tunnel] ERSPANHardwareId= (.netdev)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#ERSPANHardwareId=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/tunnel.c#L1103-L1129   config_parse_erspan_hwid
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.h#L473-L498   config_parse_uint16_bounded
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L2151-L2181   config_parse_unsigned_bounded
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L480-L495   safe_atou_bounded
 */

/**
 * Validator for [Tunnel] ERSPANHardwareId= (.netdev).
 *
 * C Function: config_parse_erspan_hwid(0). An empty assignment resets the setting and is handled before
 * this grammar runs.
 *
 * config_parse_uint16_bounded(…, 0, 63, …) reads the value with safe_atou() (base 0), so hex (`0x1f`),
 * octal (`077`, `0o77`), binary (`0b1`) and a leading `+` are all accepted, as long as the value is at
 * most 63 (sd-check: `0x40` is "allowed range is 0..63").
 */
class ConfigParseErspanHwidOptionValue : SimpleGrammarOptionValues(
  "config_parse_erspan_hwid",
  SequenceCombinator(
    SafeAtoTerminal(
      signed = false,
      base = 0,
      flags = emptySet(),
      minInclusive = 0L,
      maxInclusive = 63L,
      acceptsWhitespace = true,
      acceptsMinus = true,
    ),
    EOF(),
  ),
)
