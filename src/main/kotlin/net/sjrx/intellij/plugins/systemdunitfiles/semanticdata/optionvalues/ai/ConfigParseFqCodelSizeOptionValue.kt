package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for the [FairQueueingControlledDelay] size-style options: MemoryLimitBytes=,
 * QuantumBytes= (and the deprecated MemoryLimit / Quantum aliases).
 *
 * C function: config_parse_fq_codel_size(QDISC_KIND_FQ_CODEL) in src/network/tc/fq-codel.c.
 * After branching on the lvalue it calls parse_size(rvalue, 1024, &sz). Same IEC byte shape
 * as the other parse_size-based validators.
 */
class ConfigParseFqCodelSizeOptionValue : SimpleGrammarOptionValues(
    "config_parse_fq_codel_size",
    SequenceCombinator(
        OptionalWhitespacePrefix(BYTES),
        EOF()
    )
)
