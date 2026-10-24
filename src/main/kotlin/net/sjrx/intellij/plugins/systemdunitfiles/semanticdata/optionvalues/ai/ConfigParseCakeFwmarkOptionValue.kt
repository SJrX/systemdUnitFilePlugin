package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * [CAKE] FirewallMark= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#FirewallMark=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/cake.c#L556-L612   config_parse_cake_fwmark
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full (safe_atou32 / safe_atou)
 */

/**
 * Validator for [CAKE] FirewallMark= (.network).
 *
 * C Function: config_parse_cake_fwmark(QDISC_KIND_CAKE). An empty assignment resets it and is handled
 * before this grammar runs. The value is read with safe_atou32() (base 0, so `0xff00` is accepted;
 * sd-check at 0f0682b773) and must not be 0.
 */
class ConfigParseCakeFwmarkOptionValue : SimpleGrammarOptionValues(
  "config_parse_cake_fwmark",
  SequenceCombinator(
    SafeAtoTerminal(
      signed = false,
      base = 0,
      flags = emptySet(),
      minInclusive = 1L,
      maxInclusive = 4_294_967_295L,
      acceptsWhitespace = true,
      acceptsMinus = true,
    ),
    EOF(),
  ),
)
