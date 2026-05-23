package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for the HTB class size-style options: QuantumBytes=, MTUBytes=, OverheadBytes=,
 * BufferBytes=, CeilBufferBytes=.
 *
 * C function: config_parse_htb_class_size(TCLASS_KIND_HTB) in src/network/tc/htb.c. After
 * branching on the lvalue, it calls parse_size(rvalue, 1024, &v), so the value is a decimal
 * byte count optionally suffixed with an IEC unit. Same shape as config_parse_tbf_size and
 * config_parse_fq_size.
 *
 * The per-lvalue OverheadBytes <= UINT16_MAX bound and the general v <= UINT32_MAX bound are
 * semantic checks that can't be expressed at the grammar level without lvalue access.
 */
class ConfigParseHtbClassSizeOptionValue : SimpleGrammarOptionValues(
    "config_parse_htb_class_size",
    SequenceCombinator(
        OptionalWhitespacePrefix(BYTES),
        EOF()
    )
)
