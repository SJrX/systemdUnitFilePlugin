package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for the TokenBucketFilter size-style options: BurstBytes, LimitBytes, MTUBytes,
 * MPUBytes (and the deprecated Burst / LimitSize aliases).
 *
 * C function: config_parse_tbf_size(QDISC_KIND_TBF) in src/network/tc/tbf.c. After branching
 * on the lvalue, it calls parse_size(rvalue, 1024, &k), so the value is a decimal byte count
 * optionally suffixed with an IEC unit. Mirrors the existing config_parse_fq_size validator.
 */
class ConfigParseTbfSizeOptionValue : SimpleGrammarOptionValues(
    "config_parse_tbf_size",
    SequenceCombinator(
        OptionalWhitespacePrefix(BYTES),
        EOF()
    )
)
