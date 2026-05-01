package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for FairQueueing.QuantumBytes, FairQueueing.InitialQuantumBytes (and the deprecated
 * Quantum / InitialQuantum aliases).
 *
 * C function: config_parse_fq_size (QDISC_KIND_FQ) in src/network/tc/fq.c. Internally calls
 * parse_size(rvalue, 1024, &sz), so the value is a decimal byte count optionally suffixed with
 * an IEC unit (B, K, M, G, T, P, E). Hexadecimal and octal forms are not accepted.
 */
class ConfigParseFairQueueingSizeOptionValue : SimpleGrammarOptionValues(
    "config_parse_fq_size",
    SequenceCombinator(
        OptionalWhitespacePrefix(BYTES),
        EOF()
    )
)
