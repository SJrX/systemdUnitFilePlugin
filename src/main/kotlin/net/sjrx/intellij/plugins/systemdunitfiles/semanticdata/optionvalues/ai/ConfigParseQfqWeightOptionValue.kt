package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * [QuickFairQueueingClass] Weight= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Weight=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/qfq.c#L55-L113   config_parse_qfq_weight (QFQ_MAX_WEIGHT = 1 << 10, qfq.c#L13)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full (safe_atou32 / safe_atou)
 */

/**
 * Validator for [QuickFairQueueingClass] Weight= (.network).
 *
 * C Function: config_parse_qfq_weight(TCLASS_KIND_QFQ). An empty assignment resets it and is handled
 * before this grammar runs. The value is read with safe_atou32() (base 0) and must be 1…QFQ_MAX_WEIGHT,
 * where QFQ_MAX_WEIGHT is 1 << 10 = 1024; so 1024 itself is accepted (sd-check at 0f0682b773; the old
 * validator stopped at 1023).
 */
class ConfigParseQfqWeightOptionValue : SimpleGrammarOptionValues(
  "config_parse_qfq_weight",
  SequenceCombinator(
    SafeAtoTerminal(
      signed = false,
      base = 0,
      flags = emptySet(),
      minInclusive = 1L,
      maxInclusive = 1024L,
      acceptsWhitespace = true,
      acceptsMinus = true,
    ),
    EOF(),
  ),
)
