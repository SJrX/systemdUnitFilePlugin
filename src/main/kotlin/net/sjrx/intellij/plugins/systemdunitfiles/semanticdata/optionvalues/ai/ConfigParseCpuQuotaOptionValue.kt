package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for CPUQuota=.
 *
 * C function: config_parse_cpu_quota in src/core/load-fragment.c. After the empty-input check
 * it calls parse_permyriad_unbounded(rvalue), which accepts N% / N.N% / N.NN% (with no upper
 * bound — values over 100% are meaningful here, e.g. "200%" means 2 cores). The result must
 * be strictly greater than zero, so "0%", "0.0%", "0.00%" are rejected at the C semantic
 * layer; the grammar accepts them but the runtime parser will warn.
 *
 * Only the ASCII percent form is matched here; the ‰/‱ Unicode suffixes are vanishingly rare
 * in unit files.
 */
class ConfigParseCpuQuotaOptionValue : SimpleGrammarOptionValues(
    "config_parse_cpu_quota",
    SequenceCombinator(
        RegexTerminal("[0-9]+(\\.[0-9]{1,2})?", "[0-9]+(\\.[0-9]{1,2})?"),
        LiteralChoiceTerminal("%"),
        EOF()
    )
)
