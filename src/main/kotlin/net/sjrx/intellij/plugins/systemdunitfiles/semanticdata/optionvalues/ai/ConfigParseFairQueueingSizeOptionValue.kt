package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import java.math.BigInteger

/*
 * [FairQueueing] QuantumBytes=, InitialQuantumBytes= (and the old Quantum=, InitialQuantum= spellings) (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#QuantumBytes=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/fq.c#L170-L236   config_parse_fq_size
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L157-L283   parse_size
 */

/**
 * Validator for [FairQueueing] QuantumBytes= and InitialQuantumBytes= (and the old Quantum= and
 * InitialQuantum= spellings).
 *
 * C function: config_parse_fq_size (QDISC_KIND_FQ). An empty assignment resets it and is handled before
 * this grammar runs. parse_size(rvalue, 1024, …) accepts fractions (`10.5M`, `10.M`) and several
 * descending elements (`1G 512M`, `3G1023M`); see [parseSize]. The result must then fit in a uint32, so
 * `4G` and `1T` are refused ("is too large"). All confirmed with sd-check at 0f0682b773.
 */
class ConfigParseFairQueueingSizeOptionValue : SimpleGrammarOptionValues(
  "config_parse_fq_size",
  SequenceCombinator(
    parseSize(
      base = 1024,
      minInclusive = BigInteger.ZERO,
      maxInclusive = SIZE_UINT32_MAX,
      allowInnerWhitespace = true,
    ),
    EOF(),
  ),
)
