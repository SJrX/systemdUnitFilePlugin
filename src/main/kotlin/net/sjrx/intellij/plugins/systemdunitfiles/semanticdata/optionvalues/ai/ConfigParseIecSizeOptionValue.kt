package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for generic IEC byte-size options parsed by config_parse_iec_size.
 *
 * C function: config_parse_iec_size in src/shared/conf-parser.c. Delegates to
 * parse_size(rvalue, 1024, &v) and additionally requires the result to fit in a size_t.
 * Same IEC byte syntax used by tbf_size / fq_size / htb_class_size.
 */
class ConfigParseIecSizeOptionValue : SimpleGrammarOptionValues(
    "config_parse_iec_size",
    SequenceCombinator(
        OptionalWhitespacePrefix(BYTES),
        EOF()
    )
)
